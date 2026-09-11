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
 * Configurable provider adapter with gateway fallback.
 *
 * Important: Sanka is no longer the only route. Providers that have another
 * maintained public adapter are tried through that adapter first, then the
 * Sanka wrapper is used as a fallback. This keeps a gateway outage from
 * taking down every provider at once.
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

    private val sankaBaseUrl = "https://www.sankavollerei.web.id/anime"
    private val wajikBaseUrl = "https://wajik-anime-api.vercel.app"
    private val kumanimeBaseUrl = "https://kumanime.vercel.app/api"

    override suspend fun search(query: String): List<ProviderAnime> {
        for (url in searchGatewayUrls(query)) {
            val root = requestJson(url) ?: continue
            val result = extractItems(root).mapNotNull { it.toAnime() }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:").trim('/')
        for (url in detailGatewayUrls(slug)) {
            val root = requestJson(url) ?: continue
            root.toAnime(slug)?.let { return it }
        }
        return null
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:").trim('/')
        for (url in detailGatewayUrls(slug)) {
            val root = requestJson(url) ?: continue
            val episodes = extractEpisodeItems(root).mapNotNull { item ->
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
        val slug = animeId.removePrefix("$id:").trim('/')
        val episodeId = getEpisodes("$id:$slug").firstOrNull { it.number == episodeNumber }?.id
            ?.removePrefix("$id:")?.trim('/') ?: episodeNumber.toString()

        for (url in episodeGatewayUrls(slug, episodeId, episodeNumber)) {
            val root = requestJson(url) ?: continue
            val streams = mutableListOf<ProviderStream>()
            collectStreams(root, streams)
            val result = streams.distinctBy { it.url }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    private fun searchGatewayUrls(query: String): List<String> = buildList {
        val encoded = encode(query)
        when (id) {
            "kuramanime", "oploverz" -> add("$wajikBaseUrl/$sourceSlug/search?q=$encoded")
            "animeindo" -> add("$kumanimeBaseUrl/search/$encoded")
        }
        add("$sankaBaseUrl/$sourceSlug/search/$encoded")
    }

    private fun detailGatewayUrls(slug: String): List<String> = buildList {
        val encoded = encodePath(slug)
        when (id) {
            "kuramanime", "oploverz" -> add("$wajikBaseUrl/$sourceSlug/anime/$encoded")
            "animeindo" -> add("$kumanimeBaseUrl/anime/$encoded")
        }
        add("$sankaBaseUrl/$sourceSlug/detail/$encoded")
    }

    private fun episodeGatewayUrls(slug: String, episodeId: String, episodeNumber: Int): List<String> = buildList {
        val encodedEpisode = encodePath(episodeId)
        when (id) {
            "kuramanime", "oploverz" -> {
                add("$wajikBaseUrl/$sourceSlug/episode/$encodedEpisode")
                add("$wajikBaseUrl/$sourceSlug/episode/$episodeNumber")
            }
            "animeindo" -> {
                add("$kumanimeBaseUrl/episode/$encodedEpisode")
                add("$kumanimeBaseUrl/episode/$slug-$episodeNumber")
            }
        }
        add("$sankaBaseUrl/$sourceSlug/episode/$encodedEpisode")
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
        val directKeys = listOf("results", "anime", "items", "search", "list", "animeList")
        for (key in directKeys) root.optJSONArray(key)?.let { return it.objects() }
        root.optJSONObject("data")?.let { nested ->
            extractItems(nested).takeIf { it.isNotEmpty() }?.let { return it }
        }
        return if (root.has("title") || root.has("name")) listOf(root) else emptyList()
    }

    private fun extractEpisodeItems(root: JSONObject): List<JSONObject> {
        val directKeys = listOf("episodes", "episode", "episodeList", "episode_list", "data", "episodeListData")
        for (key in directKeys) root.optJSONArray(key)?.let { return it.objects() }
        root.optJSONObject("data")?.let { nested ->
            extractEpisodeItems(nested).takeIf { it.isNotEmpty() }?.let { return it }
        }
        return emptyList()
    }

    private fun JSONObject.toAnime(fallbackId: String? = null): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle") ?: return null
        val rawId = firstString("slug", "endpoint", "id", "animeId", "animeId") ?: fallbackId ?: title.slugify()
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
            year = firstString("year", "released", "release")?.let {
                Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull()
            },
            status = firstString("status") ?: "UNKNOWN",
            rating = firstString("rating", "score", "skor")?.toDoubleOrNull(),
            latestEpisode = episodes.mapNotNull { it.episodeNumber() }.maxOrNull()
        )
    }

    private fun collectStreams(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4", "source", "videoUrl")
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
            return buildList {
                for (i in 0 until array.length()) {
                    array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add)
                }
            }
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
