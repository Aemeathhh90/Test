package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Samehadaku adapter backed by a public streaming API wrapper.
 *
 * The adapter normalizes the wrapper response into KakaAnime's provider model
 * so the player and router do not depend on Samehadaku-specific JSON.
 */
class SamehadakuProvider : AnimeProvider {

    override val id = "samehadaku"
    override val name = "Samehadaku"
    override val priority = 20

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://www.keyrafara.com/streaming/samehadaku"

    override suspend fun search(query: String): List<ProviderAnime> {
        val root = requestJson("$baseUrl?query=${encode(query)}") ?: return emptyList()
        return extractAnimeArray(root).mapNotNull { it.toProviderAnime() }
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val root = requestForAnime(animeId) ?: return null
        return root.toProviderAnime()
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val root = requestForAnime(animeId) ?: return emptyList()
        return extractEpisodeArray(root).mapNotNull { item ->
            val number = item.episodeNumber() ?: return@mapNotNull null
            val endpoint = item.optString("slug").ifBlank {
                item.optString("id").ifBlank { "$number" }
            }
            ProviderEpisode(
                id = "$id:$endpoint",
                animeId = normalizeAnimeId(animeId),
                number = number,
                providerId = id,
                title = item.optString("title").ifBlank { "Episode $number" },
                isNew = item.optBoolean("isNew", false)
            )
        }.distinctBy { it.number }.sortedBy { it.number }
    }

    override suspend fun getStreams(
        animeId: String,
        episodeNumber: Int
    ): List<ProviderStream> {
        val root = requestForAnime(animeId, episodeNumber) ?: return emptyList()
        val streams = mutableListOf<ProviderStream>()

        collectUrls(root, streams)

        return streams
            .filter { it.url.startsWith("http", ignoreCase = true) }
            .distinctBy { it.url }
    }

    private suspend fun requestForAnime(animeId: String, episodeNumber: Int? = null): JSONObject? =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = normalizeAnimeId(animeId).removePrefix("$id:")
                val url = buildString {
                    append(baseUrl)
                    append("?query=")
                    append(encode(query))
                    if (episodeNumber != null) {
                        append("&episode=")
                        append(episodeNumber)
                    }
                }
                requestJsonBlocking(url)
            }.getOrNull()
        }

    private fun requestJson(url: String): JSONObject? = requestJsonBlocking(url)

    private fun requestJsonBlocking(url: String): JSONObject? = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "KakaAnime/0.1")
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) null else JSONObject(body)
        }
    }.getOrNull()

    private fun JSONObject.toProviderAnime(): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle") ?: return null
        val rawId = firstString("slug", "id", "animeId") ?: title.slugify()
        val episodes = extractEpisodeArray(this)
        return ProviderAnime(
            id = "$id:$rawId",
            title = title,
            providerId = id,
            posterUrl = firstString("poster", "posterUrl", "image", "thumbnail", "thumb"),
            description = firstString("description", "synopsis") ?: "",
            genres = extractStringArray(this, "genres"),
            year = firstString("year", "released", "release")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() },
            status = firstString("status") ?: "UNKNOWN",
            rating = firstString("rating", "score")?.toDoubleOrNull(),
            latestEpisode = episodes.mapNotNull { it.episodeNumber() }.maxOrNull()
        )
    }

    private fun extractAnimeArray(root: JSONObject): List<JSONObject> {
        val candidates = listOf("results", "data", "anime", "search", "items")
        for (key in candidates) {
            val array = root.optJSONArray(key)
            if (array != null) return array.objects()
        }
        return if (root.has("title") || root.has("animeTitle")) listOf(root) else emptyList()
    }

    private fun extractEpisodeArray(root: JSONObject): List<JSONObject> {
        val candidates = listOf("episodes", "episodeList", "episode_list", "data")
        for (key in candidates) {
            val array = root.optJSONArray(key)
            if (array != null) return array.objects()
        }
        return emptyList()
    }

    private fun collectUrls(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4")
                for (key in directKeys) {
                    val url = value.optString(key).trim()
                    if (url.startsWith("http", ignoreCase = true)) {
                        out += streamFromUrl(url, value.optString("quality").ifBlank { quality })
                    }
                }
                val nextQuality = value.optString("quality").ifBlank { quality }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val child = value.opt(key)
                    if (key !in directKeys) collectUrls(child, out, nextQuality)
                }
            }
            is JSONArray -> {
                for (i in 0 until value.length()) collectUrls(value.opt(i), out, quality)
            }
        }
    }

    private fun streamFromUrl(url: String, quality: String?): ProviderStream =
        ProviderStream(
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

    private fun JSONObject.firstString(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> optString(key).trim().ifBlank { null } }

    private fun JSONObject.episodeNumber(): Int? =
        firstString("episode", "episodeNumber", "number", "episodeNum")?.toIntOrNull()
            ?: Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE)
                .find(firstString("title", "name", "slug", "id").orEmpty())
                ?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun extractStringArray(root: JSONObject, key: String): List<String> {
        val array = root.optJSONArray(key) ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val value = array.optString(i).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = buildList {
        for (i in 0 until length()) optJSONObject(i)?.let(::add)
    }

    private fun normalizeAnimeId(value: String): String =
        value.removePrefix("$id:").trim()

    private fun String.slugify(): String =
        trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
}
