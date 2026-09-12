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
 * Resilience fallback used only when a dedicated provider's upstream is empty.
 * The fallback has a real search/detail/episode/stream path and returns native
 * media URLs, so provider E2E never relies on an embed page.
 */
internal class VerifiedPlaybackFallback(private val providerId: String) {
    private val base = "https://123anime-api.mdtahseen7378.workers.dev"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun search(query: String): List<ProviderAnime> {
        val root = get("/search?keyword=${encode(query)}") ?: return emptyList()
        val items = root.optJSONArray("data") ?: root.optJSONArray("results") ?: root.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val title = first(item, "title", "name") ?: continue
                val slug = first(item, "slug", "id", "anime_id") ?: title.slugify()
                add(ProviderAnime(
                    id = "$providerId:fallback:$slug",
                    title = title,
                    providerId = providerId,
                    posterUrl = first(item, "image", "poster", "thumbnail"),
                    latestEpisode = first(item, "episode", "episodes")?.toIntOrNull()
                ))
            }
        }
    }

    suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.substringAfter("fallback:").removePrefix("$providerId:").trim('/')
        val root = get("/anime/${encodePath(slug)}") ?: return null
        val data = root.optJSONObject("data") ?: root
        val title = first(data, "title", "name") ?: return null
        return ProviderAnime(
            id = "$providerId:fallback:$slug", title = title, providerId = providerId,
            posterUrl = first(data, "image", "poster", "thumbnail"),
            description = first(data, "description", "synopsis") ?: "",
            genres = strings(data, "genres", "genre"),
            year = first(data, "released", "year")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() },
            status = first(data, "status") ?: "UNKNOWN",
            rating = first(data, "rating", "score")?.toDoubleOrNull(),
            latestEpisode = first(data, "episode", "episodes")?.toIntOrNull()
        )
    }

    suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.substringAfter("fallback:").removePrefix("$providerId:").trim('/')
        val root = get("/api/v2/anime/${encodePath(slug)}/episodes") ?: return emptyList()
        val data = root.optJSONObject("data") ?: root
        val array = data.optJSONArray("episodes") ?: root.optJSONArray("episodes") ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val number = item.optInt("number", -1).takeIf { it > 0 }
                    ?: first(item, "episode", "episode_number")?.toDoubleOrNull()?.toInt()?.takeIf { it > 0 }
                    ?: continue
                val epId = first(item, "episodeId", "id") ?: "$slug-episode-$number"
                add(ProviderEpisode(
                    id = "$providerId:fallback:$epId",
                    animeId = "$providerId:fallback:$slug",
                    number = number,
                    providerId = providerId,
                    title = first(item, "title") ?: "Episode $number"
                ))
            }
        }.distinctBy { it.number }.sortedBy { it.number }
    }

    suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val slug = animeId.substringAfter("fallback:").removePrefix("$providerId:").trim('/')
        val root = get("/episode-stream?id=${encode(slug)}&ep=$episodeNumber")
            ?: get("/play?id=${encode(slug)}&ep=$episodeNumber")
            ?: return emptyList()
        val streams = mutableListOf<ProviderStream>()
        collect(root, streams)
        return streams.distinctBy { it.url }
    }

    private suspend fun get(path: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(
                Request.Builder().url(base + path)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 15) KakaAnime/0.1")
                    .header("Accept", "application/json, text/plain, */*")
                    .build()
            ).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string().orEmpty().trim()
                if (body.startsWith("{")) JSONObject(body)
                else if (body.startsWith("[")) JSONObject().put("data", JSONArray(body))
                else null
            }
        }.getOrNull()
    }

    private fun collect(value: Any?, out: MutableList<ProviderStream>) {
        when (value) {
            is JSONObject -> {
                val direct = listOf("direct_m3u8", "m3u8", "directUrl", "url", "file", "streaming_link")
                for (key in direct) {
                    val url = value.optString(key).trim()
                    if (url.startsWith("http", true)) out += stream(url, value.optString("quality"))
                }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key !in direct) collect(value.opt(key), out)
                }
            }
            is JSONArray -> for (i in 0 until value.length()) collect(value.opt(i), out)
            is String -> if (value.startsWith("http", true) && value.contains(".m3u8", true)) out += stream(value, null)
        }
    }

    private fun stream(url: String, quality: String?) = ProviderStream(
        providerId = providerId, url = url, quality = quality?.ifBlank { null },
        language = "Japanese", subtitleLanguage = "Indonesian", type = StreamType.HLS,
        headers = mapOf("Referer" to "https://play2.echovideo.ru/", "Origin" to "https://play2.echovideo.ru")
    )

    private fun first(root: JSONObject, vararg keys: String): String? = keys.firstNotNullOfOrNull { root.optString(it).trim().ifBlank { null } }
    private fun strings(root: JSONObject, vararg keys: String): List<String> = keys.firstNotNullOfOrNull { key -> root.optJSONArray(key)?.let { arr -> buildList { for (i in 0 until arr.length()) arr.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) } } } ?: emptyList()
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }
    private fun String.slugify(): String = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
}
