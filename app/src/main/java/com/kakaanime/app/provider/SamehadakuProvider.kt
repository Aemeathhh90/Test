package com.kakaanime.app.provider

import com.kakaanime.app.provider.extractor.ExtractorRegistry
import com.kakaanime.app.provider.extractor.StreamResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Samehadaku adapter following the CloudStream-style source flow:
 * HTML search/detail/episode discovery first, then episode-page -> extractor
 * resolution. JSON gateways remain fallback-only for resilience.
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
        .build()

    private val mainUrl = "https://v2.samehadaku.how"
    private val primary = "https://www.keyrafara.com/streaming/samehadaku"
    private val wajik = "https://wajik-anime-api.vercel.app/samehadaku"
    private val resolver = StreamResolver(ExtractorRegistry())

    override suspend fun search(query: String): List<ProviderAnime> {
        val normalized = query.trim()
        if (normalized.isBlank()) return emptyList()

        // CloudStream reference: app.get("$mainUrl/?s=$query").document
        // then map the site's anime cards into search responses.
        val document = requestDocument("$mainUrl/?s=${encode(normalized)}")
        if (document != null) {
            val results = document
                .select("div.animepost, article.animpost")
                .mapNotNull { it.toProviderAnimeSearch() }
                .distinctBy { it.id }
            if (results.isNotEmpty()) return results
        }

        // Gateway fallback retained, but no longer the primary search path.
        return searchGateway(normalized)
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val raw = normalizeAnimeId(animeId)
        val url = when {
            raw.startsWith("http", true) -> raw
            raw.startsWith("/anime/", true) -> "$mainUrl$raw"
            else -> "$mainUrl/anime/${raw.trim('/')}/"
        }

        val document = requestDocument(url)
        if (document != null) {
            document.toProviderAnimeDetail()?.let { return it }
        }

        return getAnimeGateway(raw)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val raw = normalizeAnimeId(animeId)
        val url = when {
            raw.startsWith("http", true) -> raw
            raw.startsWith("/anime/", true) -> "$mainUrl$raw"
            else -> "$mainUrl/anime/${raw.trim('/')}/"
        }

        val document = requestDocument(url)
        if (document != null) {
            val episodes = document
                .select("div.lstepsiode.listeps ul li, div.listeps ul li, div.episodelist ul li")
                .mapNotNull { element -> element.toProviderEpisode(raw) }
                .distinctBy { it.number }
                .sortedBy { it.number }
            if (episodes.isNotEmpty()) return episodes
        }

        return getEpisodesGateway(raw)
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val rawAnimeId = normalizeAnimeId(animeId)
        val episodes = getEpisodes("$id:$rawAnimeId")
        val episode = episodes.firstOrNull { it.number == episodeNumber }

        // CloudStream reference passes the episode page itself to loadExtractor
        // via loadLinks(). This is the important difference from the old JSON
        // gateway path: server/download discovery stays inside the resolver.
        val episodeUrl = episode?.id?.removePrefix("$id:")
        if (!episodeUrl.isNullOrBlank() && episodeUrl.startsWith("http", true)) {
            val resolved = resolver.resolve(
                urls = listOf(episodeUrl),
                referer = "$mainUrl/"
            )
            if (resolved.isNotEmpty()) return resolved.map { it.copy(providerId = id) }
        }

        // If the episode list could not expose a URL, try the known slug form.
        val fallbackEpisodeUrl = "$mainUrl/one-piece-episode-$episodeNumber/"
        if (episode == null && rawAnimeId.contains("one-piece", true)) {
            val resolved = resolver.resolve(
                urls = listOf(fallbackEpisodeUrl),
                referer = "$mainUrl/"
            )
            if (resolved.isNotEmpty()) return resolved.map { it.copy(providerId = id) }
        }

        // Gateway remains a last-resort compatibility path.
        return getStreamsGateway(rawAnimeId, episodeNumber)
    }

    private suspend fun requestDocument(url: String): Document? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; KakaAnime/0.1)")
                .header("Accept", "text/html,application/xhtml+xml")
                .header("Accept-Language", "id-ID,id;q=0.9,en;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null
                else response.body?.string()?.takeIf { it.isNotBlank() }?.let { Jsoup.parse(it, url) }
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
                if (!response.isSuccessful) null
                else response.body?.string()?.takeIf { it.isNotBlank() }?.let(::JSONObject)
            }
        }.getOrNull()
    }

    private fun Element.toProviderAnimeSearch(): ProviderAnime? {
        val link = selectFirst("div.title a, div.tt a, a") ?: return null
        val title = selectFirst("div.title h2, div.tt h4")?.text()?.trim()
            ?: link.attr("title").trim().ifBlank { link.text().trim() }
        if (title.isBlank()) return null
        val href = link.absUrl("href").ifBlank { link.attr("href") }
        if (href.isBlank()) return null
        return ProviderAnime(
            id = "$id:${href.removeSuffix("/")}",
            title = title,
            providerId = id,
            posterUrl = selectFirst("div.content-thumb img, div.limit img, img")?.absUrl("src")?.ifBlank { null }
                ?: selectFirst("img")?.absUrl("data-src")?.ifBlank { null }
        )
    }

    private fun Document.toProviderAnimeDetail(): ProviderAnime? {
        val title = selectFirst("h1.entry-title")?.text()?.trim() ?: return null
        val canonical = selectFirst("link[rel=canonical]")?.attr("href")
            ?.ifBlank { null } ?: location()
        val slug = canonical.removeSuffix("/")
        val poster = selectFirst("div.thumb > img, div.fotoanime > img")?.let {
            it.absUrl("src").ifBlank { it.absUrl("data-src") }
        }
        val description = select("div.desc p, div.entry-content p, div.sinopc p").text().trim()
        val genres = select("div.genre-info > a, div.infozingle a[href*=/genre/]")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val status = selectFirst("div.spe > span:contains(Status), div.infozingle > p:contains(Status)")
            ?.text()?.substringAfter(":")?.trim().ifNullOrBlank("UNKNOWN")
        val rating = selectFirst("span.ratingValue, div.rating strong")?.text()
            ?.replace("Rating", "", ignoreCase = true)?.trim()?.toDoubleOrNull()
        val year = Regex("\\b(19\\d{2}|20\\d{2})\\b")
            .find(select("div.spe, div.infozingle").text())?.value?.toIntOrNull()
        val episodes = select("div.lstepsiode.listeps ul li, div.listeps ul li, div.episodelist ul li")
            .mapNotNull { it.toProviderEpisode(slug) }
            .map { it.number }
        return ProviderAnime(
            id = "$id:$slug",
            title = title,
            providerId = id,
            posterUrl = poster?.ifBlank { null },
            description = description,
            genres = genres,
            year = year,
            status = status,
            rating = rating,
            latestEpisode = episodes.maxOrNull()
        )
    }

    private fun Element.toProviderEpisode(animeId: String): ProviderEpisode? {
        val anchor = selectFirst("span.lchx > a, a") ?: return null
        val title = anchor.text().trim().ifBlank { "Episode" }
        val number = Regex("(?:Episode|Ep|Eps)\\s*([0-9]+(?:\\.[0-9]+)?)", RegexOption.IGNORE_CASE)
            .find(title)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.let { it.toInt() }
            ?: Regex("(?:episode|ep)[^0-9]*([0-9]+)", RegexOption.IGNORE_CASE)
                .find(anchor.attr("href"))?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: selectFirst("span")?.text()?.filter { it.isDigit() }?.toIntOrNull()
            ?: return null
        val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }
        if (href.isBlank()) return null
        return ProviderEpisode(
            id = "$id:$href",
            animeId = "$id:${animeId.removePrefix("$id:")}",
            number = number,
            providerId = id,
            title = if (title.equals("Episode", true)) "Episode $number" else title
        )
    }

    private suspend fun searchGateway(query: String): List<ProviderAnime> {
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

    private suspend fun getAnimeGateway(slug: String): ProviderAnime? {
        for (url in listOf(
            "$primary?query=${encode(slug)}",
            "$wajik/anime/${encodePath(slug)}"
        )) {
            val root = requestJson(url) ?: continue
            root.toProviderAnime()?.let { return it }
            extractAnimeArray(root).firstOrNull()?.toProviderAnime()?.let { return it }
        }
        return null
    }

    private suspend fun getEpisodesGateway(slug: String): List<ProviderEpisode> {
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
            }.distinctBy { it.number }.sortedBy { it.number }
            if (episodes.isNotEmpty()) return episodes
        }
        return emptyList()
    }

    private suspend fun getStreamsGateway(slug: String, episodeNumber: Int): List<ProviderStream> {
        val episodes = getEpisodesGateway(slug)
        val episode = episodes.firstOrNull { it.number == episodeNumber }
        val endpoint = episode?.id?.removePrefix("$id:") ?: episodeNumber.toString()
        for (url in listOf(
            "$primary?query=${encode(slug)}&episode=$episodeNumber",
            "$wajik/episode/${encodePath(endpoint)}",
            "$wajik/episode/$episodeNumber"
        )) {
            val root = requestJson(url) ?: continue
            val streams = mutableListOf<ProviderStream>()
            collectUrls(root, streams)
            val unique = streams.distinctBy { it.url }
            if (unique.isEmpty()) continue
            val resolved = resolver.resolve(unique.map { it.url })
                .map { it.copy(providerId = id) }
            if (resolved.isNotEmpty()) return resolved
            return unique
        }
        return emptyList()
    }

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
        for (key in listOf("episodes", "episodeList", "episode_list", "episode")) root.optJSONArray(key)?.let { return it.objects() }
        root.optJSONObject("data")?.let { nested -> extractEpisodeArray(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        return emptyList()
    }

    private fun collectUrls(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4", "source", "videoUrl")
                val nextQuality = value.firstString("quality", "resolution") ?: quality
                for (key in directKeys) value.optString(key).trim().takeIf { it.startsWith("http", true) }?.let { out += streamFromUrl(it, nextQuality) }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key !in directKeys) collectUrls(value.opt(key), out, nextQuality)
                }
            }
            is JSONArray -> for (i in 0 until value.length()) collectUrls(value.opt(i), out, quality)
            is String -> if (value.startsWith("http", true)) out += streamFromUrl(value, quality)
        }
    }

    private fun streamFromUrl(url: String, quality: String?): ProviderStream = ProviderStream(
        providerId = id,
        url = url,
        quality = quality?.ifBlank { null },
        language = "Japanese",
        subtitleLanguage = "Indonesian",
        type = when {
            url.contains(".m3u8", true) -> StreamType.HLS
            url.contains(".mpd", true) -> StreamType.DASH
            url.contains(".mp4", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    )

    private fun JSONObject.firstString(vararg keys: String): String? = keys.firstNotNullOfOrNull { optString(it).trim().ifBlank { null } }
    private fun JSONObject.episodeNumber(): Int? = firstString("episode", "episodeNumber", "number", "episodeNum")?.toIntOrNull()
        ?: Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(firstString("title", "name", "slug", "id", "judul").orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull()
    private fun extractStringArray(root: JSONObject, vararg keys: String): List<String> = keys.firstNotNullOfOrNull { key -> root.optJSONArray(key)?.let { array -> buildList { for (i in 0 until array.length()) array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) } } } ?: emptyList()
    private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
    private fun normalizeAnimeId(value: String): String = value.removePrefix("$id:").trim('/')
    private fun String.slugify(): String = trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }
    private fun String?.ifNullOrBlank(fallback: String): String = if (this.isNullOrBlank()) fallback else this
}
