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
 * Configurable adapter for public JSON anime-source wrappers.
 *
 * The adapter keeps source-specific naming outside the player/router. When a
 * source wrapper changes, only its source slug/config needs to be updated.
 * It does not bypass authentication or DRM.
 */
class RemoteSourceProvider(
    override val id: String,
    override val name: String,
    override val priority: Int,
    private val sourceSlug: String
) : AnimeProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://www.sankavollerei.web.id/anime"

    override suspend fun search(query: String): List<ProviderAnime> =
        requestJson("$baseUrl/$sourceSlug/search/${encode(query)}")
            ?.let(::extractItems)
            ?.mapNotNull { it.toAnime() }
            ?: emptyList()

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:").trim('/')
        return requestJson("$baseUrl/$sourceSlug/detail/${encodePath(slug)}")
            ?.toAnime(slug)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:").trim('/')
        val root = requestJson("$baseUrl/$sourceSlug/detail/${encodePath(slug)}") ?: return emptyList()
        return extractEpisodeItems(root).mapNotNull { item ->
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
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val endpoint = episode.id.removePrefix("$id:").trim('/')
        val root = requestJson("$baseUrl/$sourceSlug/episode/${encodePath(endpoint)}") ?: return emptyList()
        val streams = mutableListOf<ProviderStream>()
        collectStreams(root, streams)
        return streams.distinctBy { it.url }
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
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
    }

    private fun extractItems(root: JSONObject): List<JSONObject> {
        val keys = listOf("results", "data", "anime", "items", "search", "list")
        for (key in keys) root.optJSONArray(key)?.let { return it.objects() }
        return if (root.has("title") || root.has("name")) listOf(root) else emptyList()
    }

    private fun extractEpisodeItems(root: JSONObject): List<JSONObject> {
        val keys = listOf("episodes", "episode", "episodeList", "episode_list", "data")
        for (key in keys) root.optJSONArray(key)?.let { return it.objects() }
        return emptyList()
    }

    private fun JSONObject.toAnime(fallbackId: String? = null): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle") ?: return null
        val rawId = firstString("slug", "endpoint", "id", "animeId") ?: fallbackId ?: title.slugify()
        val episodes = extractEpisodeItems(this)
        return ProviderAnime(
            id = "$id:$rawId",
            title = title,
            providerId = id,
            alternativeTitles = extractStrings(this, "alternativeTitles", "alternatives"),
            posterUrl = firstString("poster", "posterUrl", "image", "thumbnail", "thumb"),
            backdropUrl = firstString("backdrop", "backdropUrl", "cover"),
            description = firstString("description", "synopsis") ?: "",
            genres = extractStrings(this, "genres", "genre"),
            year = firstString("year", "released", "release")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() },
            status = firstString("status") ?: "UNKNOWN",
            rating = firstString("rating", "score", "skor")?.toDoubleOrNull(),
            latestEpisode = episodes.mapNotNull { it.episodeNumber() }.maxOrNull()
        )
    }

    private fun collectStreams(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4", "source")
                val nextQuality = value.firstString("quality", "resolution") ?: quality
                for (key in directKeys) {
                    val url = value.optString(key).trim()
                    if (url.startsWith("http", true)) out += stream(url, nextQuality)
                }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key !in directKeys) collectStreams(value.opt(key), out, nextQuality)
                }
            }
            is JSONArray -> for (i in 0 until value.length()) collectStreams(value.opt(i), out, quality)
            is String -> if (value.startsWith("http", true)) out += stream(value, quality)
        }
    }

    private fun stream(url: String, quality: String?): ProviderStream = ProviderStream(
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
                .find(firstString("title", "name", "slug", "endpoint", "id").orEmpty())
                ?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun extractStrings(root: JSONObject, vararg keys: String): List<String> {
        for (key in keys) {
            val array = root.optJSONArray(key) ?: continue
            return buildList { for (i in 0 until array.length()) array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) }
        }
        return emptyList()
    }

    private fun JSONArray.objects(): List<JSONObject> = buildList {
        for (i in 0 until length()) optJSONObject(i)?.let(::add)
    }

    private fun String.slugify(): String = trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")

    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }
}
