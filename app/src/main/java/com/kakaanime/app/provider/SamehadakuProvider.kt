package com.kakaanime.app.provider

import com.kakaanime.app.provider.extractor.ExtractorRegistry
import com.kakaanime.app.provider.extractor.StreamResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Samehadaku adapter.
 *
 * Current site structure is HTML-first (`v2.samehadaku.how`). The older JSON
 * gateways remain as fallbacks so a temporary site/parser change does not
 * immediately remove the provider from the app.
 */
class SamehadakuProvider : AnimeProvider {
    override val id = "samehadaku"
    override val name = "Samehadaku"
    override val priority = 20

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val baseUrl = "https://v2.samehadaku.how"
    private val primary = "https://www.keyrafara.com/streaming/samehadaku"
    private val wajik = "https://wajik-anime-api.vercel.app/samehadaku"
    private val resolver = StreamResolver(ExtractorRegistry())

    override suspend fun search(query: String): List<ProviderAnime> {
        htmlSearch(query).takeIf { it.isNotEmpty() }?.let { return it }

        val urls = listOf(
            "$primary?query=${encode(query)}",
            "$wajik/search?q=${encode(query)}"
        )
        for (url in urls) {
            val root = requestJson(url) ?: continue
            val result = extractAnimeArray(root).mapNotNull { it.toProviderAnime() }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val normalized = normalizeAnimeId(animeId)

        if (normalized.startsWith("http", true)) {
            requestText(normalized)?.let { html ->
                parseAnimeDetail(normalized, html)?.let { return it }
            }
        }

        htmlSearch(normalized.replace('-', ' ')).firstOrNull()?.let { searchResult ->
            requestText(searchResult.id)?.let { html ->
                parseAnimeDetail(searchResult.id, html)?.let { return it }
            }
            return searchResult
        }

        for (url in listOf(
            "$primary?query=${encode(normalized)}",
            "$wajik/anime/${encodePath(normalized)}"
        )) {
            val root = requestJson(url) ?: continue
            root.toProviderAnime()?.let { return it }
            extractAnimeArray(root).firstOrNull()?.toProviderAnime()?.let { return it }
        }
        return null
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val normalized = normalizeAnimeId(animeId)

        if (normalized.startsWith("http", true)) {
            requestText(normalized)?.let { html ->
                val episodes = parseEpisodes(normalized, html)
                if (episodes.isNotEmpty()) return episodes
            }
        }

        val animeUrl = if (normalized.startsWith("http", true)) normalized
        else "$baseUrl/anime/${normalized.trim('/')}/"

        requestText(animeUrl)?.let { html ->
            val episodes = parseEpisodes(animeUrl, html)
            if (episodes.isNotEmpty()) return episodes
        }

        val slug = normalized.substringAfterLast('/').trim('/')
        for (url in listOf(
            "$primary?query=${encode(slug)}",
            "$wajik/anime/${encodePath(slug)}"
        )) {
            val root = requestJson(url) ?: continue
            val episodes = extractEpisodeArray(root).mapNotNull { item ->
                val number = item.episodeNumber() ?: return@mapNotNull null
                val endpoint = item.firstString("slug", "endpoint", "id", "url") ?: number.toString()
                ProviderEpisode(
                    id = "$id:$endpoint",
                    animeId = "$id:$slug",
                    number = number,
                    providerId = id,
                    title = item.firstString("title", "name") ?: "Episode $number",
                    isNew = item.optBoolean("isNew", false)
                )
            }.distinctBy { it.number }.sortedBy { it.number }.toList()
            if (episodes.isNotEmpty()) return episodes
        }
        return emptyList()
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val normalized = normalizeAnimeId(animeId)
        val episodes = getEpisodes(normalized)
        val episode = episodes.firstOrNull { it.number == episodeNumber }
        val endpoint = episode?.id?.removePrefix("$id:")

        // Prefer the real Samehadaku episode page. The current site exposes
        // player/iframe data there, which can be sent through our common
        // extractor chain instead of inventing a Samehadaku-specific host
        // extractor.
        val episodeUrls = buildList {
            if (endpoint?.startsWith("http", true) == true) add(endpoint)
            else if (normalized.startsWith("http", true)) {
                add("$normalized${if (normalized.endsWith('/')) "" else "/"}episode-$episodeNumber/")
            } else {
                add("$baseUrl/${normalized.trim('/')}-episode-$episodeNumber/")
                add("$baseUrl/anime/${normalized.trim('/')}-episode-$episodeNumber/")
            }
        }.distinct()

        for (url in episodeUrls) {
            val html = requestText(url) ?: continue
            val candidates = extractPlayerUrls(html, url)
            if (candidates.isEmpty()) continue

            val resolved = resolver.resolve(candidates, referer = url)
                .map { it.copy(providerId = id) }
            if (resolved.isNotEmpty()) return resolved

            val direct = candidates
                .filter { it.contains(".m3u8", true) || it.contains(".mpd", true) || it.contains(".mp4", true) }
                .map { streamFromUrl(it, referer = url) }
            if (direct.isNotEmpty()) return direct
        }

        // Legacy gateways are retained as a final fallback.
        val slug = normalized.substringAfterLast('/').trim('/')
        for (url in listOf(
            "$primary?query=${encode(slug)}&episode=$episodeNumber",
            "$wajik/episode/${encodePath(endpoint ?: episodeNumber.toString())}",
            "$wajik/episode/$episodeNumber"
        )) {
            val root = requestJson(url) ?: continue
            val streams = mutableListOf<ProviderStream>()
            collectUrls(root, streams)
            val unique = streams.distinctBy { it.url }
            if (unique.isNotEmpty()) return unique
        }
        return emptyList()
    }

    private suspend fun htmlSearch(query: String): List<ProviderAnime> {
        val html = requestText("$baseUrl/?s=${encode(query)}") ?: return emptyList()
        val pattern = Regex(
            "<a[^>]+href=[\\\"'](https?://v2\\.samehadaku\\.how/anime/[^\\\"']+)[\\\"'][^>]*>(.*?)</a>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return pattern.findAll(html).mapNotNull { match ->
            val href = match.groupValues[1].trimEnd('/') + "/"
            val title = cleanHtml(match.groupValues[2]).takeIf { it.isNotBlank() }
                ?: href.substringAfter("/anime/").trim('/').replace('-', ' ')
            ProviderAnime(
                id = href,
                title = title,
                providerId = id,
                posterUrl = null
            )
        }.distinctBy { it.id }.toList()
    }

    private fun parseAnimeDetail(url: String, html: String): ProviderAnime? {
        val title = Regex(
            "<h1[^>]*>(.*?)</h1>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(html)?.groupValues?.get(1)?.let(::cleanHtml)?.trim()
            ?.removeSuffix(" Sub Indo")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val poster = Regex(
            "<img[^>]+(?:data-src|data-lazy-src|src)=[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        ).find(html)?.groupValues?.get(1)

        val description = Regex(
            "<div[^>]+(?:entry-content|desc|description)[^>]*>(.*?)</div>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(html)?.groupValues?.get(1)?.let(::cleanHtml).orEmpty()

        return ProviderAnime(
            id = url,
            title = title,
            providerId = id,
            posterUrl = poster,
            description = description,
            latestEpisode = parseEpisodes(url, html).maxOfOrNull { it.number }
        )
    }

    private fun parseEpisodes(animeUrl: String, html: String): List<ProviderEpisode> {
        val pattern = Regex(
            "<a[^>]+href=[\\\"']([^\\\"']+)[\\\"'][^>]*>(.*?)</a>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return pattern.findAll(html).mapNotNull { match ->
            val href = match.groupValues[1]
            val text = cleanHtml(match.groupValues[2])
            if (!href.contains("episode", true) && !text.contains("episode", true)) return@mapNotNull null
            val number = Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE)
                .find("$text $href")?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: return@mapNotNull null
            val absolute = absoluteUrl(href)
            ProviderEpisode(
                id = absolute,
                animeId = animeUrl,
                number = number,
                providerId = id,
                title = "Episode $number"
            )
        }.distinctBy { it.number }.sortedBy { it.number }.toList()
    }

    private fun extractPlayerUrls(html: String, pageUrl: String): List<String> {
        val result = linkedSetOf<String>()

        val directPattern = Regex(
            "https?://[^\\\"'\\s<>]+(?:\\.m3u8(?:\\?[^\\\"'\\s<>]*)?|\\.mpd(?:\\?[^\\\"'\\s<>]*)?|\\.mp4(?:\\?[^\\\"'\\s<>]*)?)",
            RegexOption.IGNORE_CASE
        )
        directPattern.findAll(html).forEach { result += it.value.replace("\\/", "/") }

        val attributePattern = Regex(
            "(?:data-src|data-video|data-content|src)=[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        )
        attributePattern.findAll(html).forEach { match ->
            val value = match.groupValues[1].replace("\\/", "/").trim()
            if (value.startsWith("http", true) || value.startsWith("//")) {
                result += absoluteUrl(value)
            }
        }

        val iframePattern = Regex(
            "<iframe[^>]+(?:src|data-src)=[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        )
        iframePattern.findAll(html).forEach { match ->
            val value = match.groupValues[1].trim()
            if (value.startsWith("http", true) || value.startsWith("//")) result += absoluteUrl(value)
        }

        return result
            .filter { it != pageUrl }
            .filterNot { it.contains("youtube.com", true) || it.contains("google.com", true) }
            .toList()
    }

    private suspend fun requestText(url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36")
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()?.takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()?.takeIf { it.isNotBlank() }?.let(::JSONObject)
            }
        }.getOrNull()
    }

    private fun streamFromUrl(url: String, referer: String): ProviderStream = ProviderStream(
        providerId = id,
        url = url,
        language = "Japanese",
        subtitleLanguage = "Indonesian",
        headers = mapOf("Referer" to referer),
        type = when {
            url.contains(".m3u8", true) -> StreamType.HLS
            url.contains(".mpd", true) -> StreamType.DASH
            url.contains(".mp4", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    )

    private fun cleanHtml(value: String): String = value
        .replace(Regex("<[^>]*>"), " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun absoluteUrl(value: String): String = when {
        value.startsWith("//") -> "https:$value"
        value.startsWith("http", true) -> value
        value.startsWith("/") -> "$baseUrl$value"
        else -> "$baseUrl/${value.trimStart('/')}"
    }

    private fun normalizeAnimeId(value: String): String = value.removePrefix("$id:").trim()
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }

    private fun JSONObject.toProviderAnime(): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle", "judul") ?: return null
        val rawId = firstString("slug", "endpoint", "id", "animeId") ?: title.slugify()
        val episodes = extractEpisodeArray(this)
        return ProviderAnime(
            id = "$id:$rawId",
            title = title,
            providerId = id,
            posterUrl = firstString("poster", "posterUrl", "image", "thumbnail", "thumb"),
            description = firstString("description", "synopsis", "sinopsis") ?: "",
            genres = extractStringArray(this, "genres", "genre"),
            year = firstString("year", "released", "release")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() },
            status = firstString("status") ?: "UNKNOWN",
            rating = firstString("rating", "score", "skor")?.toDoubleOrNull(),
            latestEpisode = episodes.mapNotNull { it.episodeNumber() }.maxOrNull()
        )
    }

    private fun extractAnimeArray(root: JSONObject): List<JSONObject> {
        for (key in listOf("results", "anime", "items", "search", "data")) root.optJSONArray(key)?.let { return it.objects() }
        root.optJSONObject("data")?.let { nested -> extractAnimeArray(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        return if (root.has("title") || root.has("animeTitle") || root.has("judul")) listOf(root) else emptyList()
    }

    private fun extractEpisodeArray(root: JSONObject): List<JSONObject> {
        for (key in listOf("episodes", "episode", "results", "data", "items")) root.optJSONArray(key)?.let { return it.objects() }
        root.optJSONObject("data")?.let { nested -> extractEpisodeArray(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        return emptyList()
    }

    private fun collectUrls(value: Any?, output: MutableList<ProviderStream>) {
        when (value) {
            is JSONObject -> {
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val child = value.opt(key)
                    if (child is String && child.startsWith("http", true)) {
                        if (child.contains(".m3u8", true) || child.contains(".mpd", true) || child.contains(".mp4", true) || child.contains("stream", true)) {
                            output += streamFromUrl(child, referer = baseUrl)
                        }
                    } else collectUrls(child, output)
                }
            }
            is JSONArray -> {
                for (i in 0 until value.length()) collectUrls(value.opt(i), output)
            }
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = buildList {
        for (i in 0 until length()) optJSONObject(i)?.let(::add)
    }

    private fun JSONObject.firstString(vararg keys: String): String? = keys.asSequence()
        .mapNotNull { key -> optString(key, "").takeIf { it.isNotBlank() } }
        .firstOrNull()

    private fun JSONObject.episodeNumber(): Int? {
        val value = firstString("episode", "episodeNumber", "number", "ep", "num", "title") ?: return null
        return Regex("(?:episode|eps|ep)?[^0-9]*(\\d+)", RegexOption.IGNORE_CASE)
            .find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractStringArray(root: JSONObject, vararg keys: String): List<String> {
        for (key in keys) {
            root.optJSONArray(key)?.let { array ->
                return buildList {
                    for (i in 0 until array.length()) array.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
            root.optString(key, "").takeIf { it.isNotBlank() }?.let { return it.split(',').map(String::trim).filter(String::isNotBlank) }
        }
        return emptyList()
    }

    private fun String.slugify(): String = lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
}
