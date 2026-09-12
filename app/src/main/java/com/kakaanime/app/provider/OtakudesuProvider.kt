package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Otakudesu adapter with current Jade + qrtzanim metadata fallback. */
class OtakudesuProvider : AnimeProvider {
    override val id = "otakudesu"
    override val name = "Otakudesu"
    override val priority = 10

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val baseUrl = "https://otakudesu-api-jade.vercel.app/api"
    private val qrtzBaseUrl = "https://qrtzanim.vercel.app/api"
    private val gateway = RemoteSourceProvider(id, name, priority, "otakudesu")

    override suspend fun search(query: String): List<ProviderAnime> {
        val current = requestJson("$baseUrl/search/${encode(query)}")
            ?.optJSONArray("search_results")?.toProviderAnimeList().orEmpty()
        val qrtz = requestJson("$qrtzBaseUrl/search?q=${encode(query)}&page=1")
            ?.optJSONObject("data")?.optJSONArray("results")?.toQrtzAnimeList().orEmpty()
        return when {
            qrtz.isNotEmpty() -> qrtz
            current.isNotEmpty() -> current
            else -> gateway.search(query)
        }
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = normalizeSlug(animeId.removePrefix("$id:"))
        requestJson("$baseUrl/anime/${encodePath(slug)}")?.optJSONObject("anime_detail")
            ?.toProviderAnime(slug)?.let { return it }
        requestJson("$qrtzBaseUrl/anime/${encodePath(slug)}")?.optJSONObject("data")
            ?.toQrtzAnime(slug)?.let { return it }
        return gateway.getAnime(animeId)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeSlug(animeId.removePrefix("$id:"))
        val direct = requestJson("$baseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("anime_detail")?.optJSONArray("episode_list")?.toProviderEpisodes(slug).orEmpty()
        if (direct.isNotEmpty()) return direct
        val qrtz = requestJson("$qrtzBaseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("data")?.optJSONArray("episodeList")?.toQrtzEpisodes(slug).orEmpty()
        if (qrtz.isNotEmpty()) return qrtz
        return gateway.getEpisodes(animeId)
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber }
            ?: return gateway.getStreams(animeId, episodeNumber)
        val endpoint = episode.id.removePrefix("$id:")
        val detail = requestJson("$baseUrl/episode/${encodePath(endpoint)}")?.optJSONObject("episode_detail")
        if (detail != null) {
            val streams = mutableListOf<ProviderStream>()
            detail.optString("stream_link").trim().takeIf { it.isNotBlank() }?.let { streams += stream(it) }
            val downloads = detail.optJSONArray("downloads") ?: JSONArray()
            for (i in 0 until downloads.length()) {
                val group = downloads.optJSONObject(i) ?: continue
                val quality = group.optString("quality").trim()
                val links = group.optJSONArray("links") ?: continue
                for (j in 0 until links.length()) {
                    val url = links.optJSONObject(j)?.optString("url")?.trim().orEmpty()
                    if (url.isNotBlank()) streams += stream(url, quality)
                }
            }
            val playable = streams.distinctBy { it.url }.filter { it.type != StreamType.UNKNOWN }
            if (playable.isNotEmpty()) return playable
        }
        return gateway.getStreams(animeId, episodeNumber)
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(Request.Builder().url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .header("Accept", "application/json, text/plain, */*").build()).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string().orEmpty().trim()
                when {
                    body.startsWith("{") -> JSONObject(body)
                    body.startsWith("[") -> JSONObject().put("items", JSONArray(body))
                    else -> null
                }
            }
        }.getOrNull()
    }

    private fun JSONArray.toProviderAnimeList(): List<ProviderAnime> = buildList {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            val slug = normalizeSlug(item.optString("endpoint").trim())
            if (slug.isBlank()) continue
            add(ProviderAnime("$id:$slug", item.optString("title").ifBlank { "Unknown Anime" }, id,
                posterUrl = item.optString("thumb").ifBlank { null },
                genres = item.optJSONArray("genres").toStringList(),
                status = item.optString("status").ifBlank { "UNKNOWN" },
                rating = item.optDoubleOrNull("rating")))
        }
    }

    private fun JSONArray.toQrtzAnimeList(): List<ProviderAnime> = buildList {
        for (i in 0 until length()) optJSONObject(i)?.toQrtzAnime()?.let(::add)
    }

    private fun JSONObject.toProviderAnime(fallbackSlug: String): ProviderAnime = ProviderAnime(
        id = "$id:$fallbackSlug", title = optString("title").ifBlank { "Unknown Anime" }, providerId = id,
        posterUrl = optString("thumb").ifBlank { null }, description = optString("synopsis"),
        status = optString("status").ifBlank { "UNKNOWN" }, rating = optDoubleOrNull("skor"),
        latestEpisode = optJSONArray("episode_list")?.let { array ->
            (0 until array.length()).mapNotNull { i ->
                val item = array.optJSONObject(i) ?: return@mapNotNull null
                episodeNumber(item.optString("title"), item.optString("endpoint"))
            }.maxOrNull()
        }
    )

    private fun JSONObject.toQrtzAnime(fallbackSlug: String? = null): ProviderAnime? {
        val title = optString("title").trim().ifBlank { return null }
        val slug = optString("slug").trim().ifBlank { fallbackSlug ?: return null }
        return ProviderAnime("$id:$slug", title, id,
            posterUrl = optString("thumbnail").ifBlank { optString("thumb").ifBlank { null } },
            genres = optJSONArray("genres").toStringList(),
            status = optString("status").ifBlank { "UNKNOWN" },
            rating = optString("score").toDoubleOrNull())
    }

    private fun JSONArray.toProviderEpisodes(slug: String): List<ProviderEpisode> = buildList {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            val endpoint = item.optString("endpoint").trim()
            val number = episodeNumber(item.optString("title"), endpoint) ?: continue
            add(ProviderEpisode("$id:${endpoint.ifBlank { "$slug-episode-$number" }}", "$id:$slug", number, id,
                item.optString("title").ifBlank { "Episode $number" }))
        }
    }.distinctBy { it.number }.sortedBy { it.number }

    private fun JSONArray.toQrtzEpisodes(slug: String): List<ProviderEpisode> = buildList {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            val endpoint = item.optString("slug").trim()
            val number = item.optString("episode").toIntOrNull() ?: episodeNumber(item.optString("title"), endpoint) ?: continue
            add(ProviderEpisode("$id:${endpoint.ifBlank { "$slug-episode-$number" }}", "$id:$slug", number, id,
                item.optString("title").ifBlank { "Episode $number" }))
        }
    }.distinctBy { it.number }.sortedBy { it.number }

    private fun stream(url: String, quality: String? = null) = ProviderStream(id, url, quality?.ifBlank { null },
        "Japanese", "Indonesian", when {
            url.contains(".m3u8", true) -> StreamType.HLS
            url.contains(".mpd", true) -> StreamType.DASH
            url.contains(".mp4", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        })

    private fun episodeNumber(title: String, endpoint: String): Int? {
        val value = "$title $endpoint"
        return Regex("(?:episode|eps)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("(?:^|-)e?(\\d+)(?:-|$)").find(endpoint)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun normalizeSlug(value: String): String {
        val raw = value.trim().trim('/')
        val normalized = if (raw.startsWith("http", true)) runCatching {
            URI(raw).path.substringAfter("/anime/", "").trim('/').ifBlank { raw.substringAfterLast('/') }
        }.getOrDefault(raw.substringAfterLast('/')) else raw
        return when {
            normalized.equals("1piece-sub-indo", true) -> "one-piece-sub-indo"
            normalized.startsWith("1piece-", true) -> "one-piece-" + normalized.removePrefix("1piece-")
            else -> normalized
        }
    }

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (!has(key) || isNull(key)) null else optString(key).toDoubleOrNull() ?: optDouble(key, Double.NaN).takeUnless { it.isNaN() }
    private fun JSONArray?.toStringList(): List<String> = if (this == null) emptyList() else buildList {
        for (i in 0 until length()) optString(i).trim().takeIf { it.isNotBlank() }?.let(::add)
    }
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.trim('/').split('/').joinToString("/") { encode(it) }
}
