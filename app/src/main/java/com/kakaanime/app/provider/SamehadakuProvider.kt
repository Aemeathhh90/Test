package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Samehadaku adapter with primary wrapper plus Wajik gateway fallback. */
class SamehadakuProvider : AnimeProvider {
    override val id = "samehadaku"
    override val name = "Samehadaku"
    override val priority = 20

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val primary = "https://www.keyrafara.com/streaming/samehadaku"
    private val wajik = "https://wajik-anime-api.vercel.app/samehadaku"

    override suspend fun search(query: String): List<ProviderAnime> {
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
        val slug = normalizeAnimeId(animeId)
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

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeAnimeId(animeId)
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

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val slug = normalizeAnimeId(animeId)
        val episodes = getEpisodes("$id:$slug")
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
            if (unique.isNotEmpty()) return unique
        }
        return emptyList()
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else {
                    val body = response.body?.string()?.trim().orEmpty()
                    if (body.isBlank()) null else when {
                        body.startsWith("[") -> JSONObject().put("items", JSONArray(body))
                        body.startsWith("{") -> JSONObject(body)
                        else -> null
                    }
                }
            }
        }.getOrNull()
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
        for (key in listOf("episodes", "episodeList", "episode_list", "episode", "items")) root.optJSONArray(key)?.let { return it.objects() }
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
}
