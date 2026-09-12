package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Samehadaku adapter with API and current-site HTML fallbacks. */
class SamehadakuProvider : AnimeProvider {
    override val id = "samehadaku"
    override val name = "Samehadaku"
    override val priority = 20

    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    private val primary = "https://www.keyrafara.com/streaming/samehadaku"
    private val wajik = "https://wajik-anime-api.vercel.app/samehadaku"
    private val site = "https://v2.samehadaku.how"
    private val gateway = RemoteSourceProvider(id, name, priority, "samehadaku")

    override suspend fun search(query: String): List<ProviderAnime> {
        val urls = listOf("$primary?query=${encode(query)}", "$wajik/search?q=${encode(query)}")
        for (url in urls) {
            val root = requestJson(url) ?: continue
            val result = extractAnimeArray(root).mapNotNull { it.toProviderAnime() }.filterNot { it.title.matches(Regex(".*(?:episode|eps|ep)\\s*\\d+.*", RegexOption.IGNORE_CASE)) }
            if (result.isNotEmpty()) return result
        }
        val html = requestText("$site/?s=${encode(query)}")
        if (html != null) {
            val result = Jsoup.parse(html, site).select("a[href*=/anime/]").mapNotNull { link ->
                val href = link.absUrl("href").trim()
                val title = link.text().trim()
                if (href.isBlank() || title.isBlank() || title.matches(Regex(".*(?:episode|eps|ep)\\s*\\d+.*", RegexOption.IGNORE_CASE))) null
                else ProviderAnime("$id:$href", title, id)
            }.distinctBy { it.id }
            if (result.isNotEmpty()) return result
        }
        return gateway.search(query)
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = normalizeAnimeId(animeId)
        for (url in listOf("$primary?query=${encode(slug)}", "$wajik/anime/${encodePath(slug)}")) {
            val root = requestJson(url) ?: continue
            root.toProviderAnime()?.let { return it }
            extractAnimeArray(root).firstOrNull()?.toProviderAnime()?.let { return it }
        }
        val htmlUrl = animeId.removePrefix("$id:").takeIf { it.startsWith("http") } ?: "$site/anime/${encodePath(slug)}/"
        requestText(htmlUrl)?.let { html ->
            val doc = Jsoup.parse(html, htmlUrl)
            val title = doc.selectFirst("h1.entry-title, .entry-title, h1")?.text()?.trim().orEmpty()
            if (title.isNotBlank()) return ProviderAnime(animeId, title, id, posterUrl = doc.selectFirst(".entry-content img, .thumb img, img")?.absUrl("src"))
        }
        return gateway.getAnime(animeId)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeAnimeId(animeId)
        for (url in listOf("$primary?query=${encode(slug)}", "$wajik/anime/${encodePath(slug)}")) {
            val root = requestJson(url) ?: continue
            val episodes = extractEpisodeArray(root).mapNotNull { item ->
                val number = item.episodeNumber() ?: return@mapNotNull null
                val endpoint = item.firstString("slug", "endpoint", "id", "url") ?: number.toString()
                ProviderEpisode("$id:$endpoint", "$id:$slug", number, id, item.firstString("title", "name") ?: "Episode $number", item.optBoolean("isNew", false))
            }.distinctBy { it.number }.sortedBy { it.number }
            if (episodes.isNotEmpty()) return episodes
        }
        val htmlUrl = animeId.removePrefix("$id:").takeIf { it.startsWith("http") } ?: "$site/anime/${encodePath(slug)}/"
        requestText(htmlUrl)?.let { html ->
            val doc = Jsoup.parse(html, htmlUrl)
            val episodes = doc.select("a[href]").mapNotNull { link ->
                val number = Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(link.text())?.groupValues?.get(1)?.toIntOrNull()
                    ?: Regex("(?:^|\\s)(\\d{1,4})(?:\\s|$)").find(link.text())?.groupValues?.get(1)?.toIntOrNull()
                val href = link.absUrl("href")
                if (number == null || href.isBlank()) null else ProviderEpisode("$id:$href", animeId, number, id, link.text().trim().ifBlank { "Episode $number" })
            }.distinctBy { it.number }.sortedBy { it.number }
            if (episodes.isNotEmpty()) return episodes
        }
        return gateway.getEpisodes("$id:$slug")
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val slug = normalizeAnimeId(animeId)
        val episodes = getEpisodes("$id:$slug")
        val episode = episodes.firstOrNull { it.number == episodeNumber }
        val endpoint = episode?.id?.removePrefix("$id:") ?: episodeNumber.toString()
        for (url in listOf("$primary?query=${encode(slug)}&episode=$episodeNumber", "$wajik/episode/${encodePath(endpoint)}", "$wajik/episode/$episodeNumber")) {
            val root = requestJson(url) ?: continue
            val streams = mutableListOf<ProviderStream>(); collectUrls(root, streams)
            if (streams.isNotEmpty()) return streams.distinctBy { it.url }
        }
        val episodeUrl = episode?.id?.removePrefix("$id:")?.takeIf { it.startsWith("http") }
        if (episodeUrl != null) {
            requestText(episodeUrl)?.let { html ->
                val streams = mutableListOf<ProviderStream>(); collectUrls(Jsoup.parse(html).html(), streams)
                if (streams.isNotEmpty()) return streams.distinctBy { it.url }
            }
        }
        return gateway.getStreams("$id:$slug", episodeNumber)
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) { runCatching { requestText(url)?.let { body -> when { body.trim().startsWith("[") -> JSONObject().put("items", JSONArray(body)); body.trim().startsWith("{") -> JSONObject(body); else -> null } } }.getOrNull() }
    private suspend fun requestText(url: String): String? = withContext(Dispatchers.IO) { runCatching { client.newCall(Request.Builder().url(url).header("User-Agent", ua).header("Accept", "application/json, text/plain, text/html, */*").header("Referer", "$site/").build()).execute().use { r -> if (r.isSuccessful) r.body?.string()?.trim().takeIf { !it.isNullOrBlank() } else null } }.getOrNull() }
    private fun JSONObject.toProviderAnime(): ProviderAnime? { val title = firstString("title", "name", "animeTitle", "judul") ?: return null; val rawId = firstString("slug", "endpoint", "id", "animeId") ?: title.slugify(); return ProviderAnime("$id:$rawId", title, id, posterUrl = firstString("poster", "posterUrl", "image", "thumbnail", "thumb"), description = firstString("description", "synopsis", "sinopsis") ?: "", genres = extractStringArray(this, "genres", "genre"), year = firstString("year", "released", "release")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() }, status = firstString("status") ?: "UNKNOWN", rating = firstString("rating", "score", "skor")?.toDoubleOrNull(), latestEpisode = extractEpisodeArray(this).mapNotNull { it.episodeNumber() }.maxOrNull()) }
    private fun extractAnimeArray(root: JSONObject): List<JSONObject> { for (key in listOf("results", "anime", "items", "search", "data", "result")) root.optJSONArray(key)?.let { return it.objects() }; for (key in listOf("data", "result")) root.optJSONObject(key)?.let { nested -> extractAnimeArray(nested).takeIf { it.isNotEmpty() }?.let { return it } }; return if (root.has("title") || root.has("animeTitle") || root.has("judul") || root.has("name")) listOf(root) else emptyList() }
    private fun extractEpisodeArray(root: JSONObject): List<JSONObject> { for (key in listOf("episodes", "episodeList", "episode_list", "episode", "items", "result")) root.optJSONArray(key)?.let { return it.objects() }; for (key in listOf("data", "result")) root.optJSONObject(key)?.let { nested -> extractEpisodeArray(nested).takeIf { it.isNotEmpty() }?.let { return it } }; return emptyList() }
    private fun collectUrls(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) { when (value) { is JSONObject -> { val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4", "source", "videoUrl", "link"); val nextQuality = value.firstString("quality", "resolution") ?: quality; for (key in directKeys) value.optString(key).trim().takeIf { it.startsWith("http", true) }?.let { out += streamFromUrl(it, nextQuality) }; val keys = value.keys(); while (keys.hasNext()) { val key = keys.next(); if (key !in directKeys) collectUrls(value.opt(key), out, nextQuality) } }; is JSONArray -> for (i in 0 until value.length()) collectUrls(value.opt(i), out, quality); is String -> Regex("https?://[^\\s\"'<>]+", RegexOption.IGNORE_CASE).findAll(value).forEach { out += streamFromUrl(it.value, quality) } } }
    private fun streamFromUrl(url: String, quality: String?): ProviderStream = ProviderStream(id, url, quality?.ifBlank { null }, "Japanese", "Indonesian", when { url.contains(".m3u8", true) -> StreamType.HLS; url.contains(".mpd", true) -> StreamType.DASH; url.contains(".mp4", true) -> StreamType.MP4; else -> StreamType.UNKNOWN })
    private fun JSONObject.firstString(vararg keys: String): String? = keys.firstNotNullOfOrNull { optString(it).trim().ifBlank { null } }
    private fun JSONObject.episodeNumber(): Int? = firstString("episode", "episodeNumber", "number", "episodeNum")?.toIntOrNull() ?: Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(firstString("title", "name", "slug", "id", "judul").orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull()
    private fun extractStringArray(root: JSONObject, vararg keys: String): List<String> = keys.firstNotNullOfOrNull { key -> root.optJSONArray(key)?.let { array -> buildList { for (i in 0 until array.length()) array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) } } } ?: emptyList()
    private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
    private fun normalizeAnimeId(value: String): String = value.removePrefix("$id:").trim('/').replace(Regex("(?:-episode|-ep|-eps)-\\d+(?:-.*)?$", RegexOption.IGNORE_CASE), "")
    private fun String.slugify(): String = trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }
    private val ua = "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36 KakaAnime/0.1"
}
