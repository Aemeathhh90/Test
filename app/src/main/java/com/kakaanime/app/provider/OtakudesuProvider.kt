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

/**
 * Otakudesu adapter. Uses the dedicated Jade wrapper first, with qrtzanim as a
 * current-site fallback for search/detail/episode metadata. Stream resolution
 * still prefers the dedicated wrapper so the strict E2E gate can reach a native
 * HLS/DASH/MP4 URL instead of an embed page.
 */
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
            ?.optJSONArray("search_results")
            ?.toProviderAnimeList()
            .orEmpty()

        val qrtz = requestJson("$qrtzBaseUrl/search?q=${encode(query)}&page=1")
            ?.optJSONObject("data")
            ?.optJSONArray("results")
            ?.toQrtzAnimeList()
            .orEmpty()

        // Prefer the current wrapper's clean slugs when available, but use the
        // qrtz result when the old wrapper returns a malformed endpoint such as
        // the observed `1piece-sub-indo` search id.
        val cleanCurrent = current.filter { isUsableSlug(it.id.removePrefix("$id:")) }
        return when {
            qrtz.isNotEmpty() -> qrtz
            cleanCurrent.isNotEmpty() -> cleanCurrent
            current.isNotEmpty() -> current
            else -> gateway.search(query)
        }
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = normalizeSlug(animeId.removePrefix("$id:"))

        requestJson("$baseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("anime_detail")
            ?.toProviderAnime(slug)
            ?.let { return it }

        requestJson("$qrtzBaseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("data")
            ?.toQrtzAnime(slug)
            ?.let { return it }

        return gateway.getAnime(animeId)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeSlug(animeId.removePrefix("$id:"))

        val direct = requestJson("$baseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("anime_detail")
            ?.optJSONArray("episode_list")
            ?.toProviderEpisodes(slug)
            .orEmpty()
        if (direct.isNotEmpty()) return direct

        val qrtz = requestJson("$qrtzBaseUrl/anime/${encodePath(slug)}")
            ?.optJSONObject("data")
            ?.optJSONArray("episodeList")
            ?.toQrtzEpisodes(slug)
            .orEmpty()
        if (qrtz.isNotEmpty()) return qrtz

        return gateway.getEpisodes(animeId)
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber }
        if (episode == null) return gateway.getStreams(animeId, episodeNumber)

        val endpoint = episode.id.removePrefix("$id:")
        val root = requestJson("$baseUrl/episode/${encodePath(endpoint)}")
        val detail = root?.optJSONObject("episode_detail")
        if (detail != null) {
            val streams = mutableListOf<ProviderStream>()
            val direct = detail.optString("stream_link").trim()
            if (direct.isNotBlank()) streams += streamFromUrl(direct)

            val downloads = detail.optJSONArray("downloads") ?: JSONArray()
            for (i in 0 until downloads.length()) {
                val qualityGroup = downloads.optJSONObject(i) ?: continue
                val quality = qualityGroup.optString("quality").trim()
                val links = qualityGroup.optJSONArray("links") ?: continue
                for (j in 0 until links.length()) {
                    val link = links.optJSONObject(j)?.optString("url")?.trim().orEmpty()
                    if (link.isNotBlank()) streams += streamFromUrl(link, quality)
                }
            }
            streams.distinctBy { it.url }
                .filter { it.type != StreamType.UNKNOWN }
                .takeIf { it.isNotEmpty() }
                ?.let { return it }
        }

        return gateway.getStreams(animeId, episodeNumber)
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .header("Accept", "application/json, text/plain, */*")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string().orEmpty().trim()
                if (body.isBlank()) null else if (body.startsWith("["))
                    JSONObject().put("items", JSONArray(body)) else JSONObject(body)
            }
        }.getOrNull()
    }

    private fun JSONObject.toProviderAnime(fallbackId: String): ProviderAnime {
        val title = optString("title").ifBlank { "Unknown Anime" }
        val episodeList = optJSONArray("episode_list")
        return ProviderAnime(
            id = "$id:$fallbackId", title = title, providerId = id,
            posterUrl = optString("thumb").ifBlank { null }, description = optString("synopsis"),
            year = extractYear(optString("release")), status = optString("status").ifBlank { "UNKNOWN" },
            rating = optDoubleOrNull("skor"), latestEpisode = episodeList?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    extractEpisodeNumber(item.optString("title"), item.optString("endpoint"))
                }.maxOrNull()
            }
        )
    }

    private fun JSONArray.toProviderAnimeList(): List<ProviderAnime> = buildList {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            val slug = normalizeSlug(item.optString("endpoint").trim())
            if (slug.isBlank()) continue
            add(ProviderAnime(
                id = "$id:$slug", title = item.optString("title").ifBlank { "Unknown Anime" },
                providerId = id, posterUrl = item.optString("thumb").ifBlank { null },
                genres = item.optJSONArray("genres").toStringList(),
                status = item.optString("status").ifBlank { "UNKNOWN" }, rating = item.optDoubleOrNull("rating")
            ))
        }
    }

    private fun JSONArray.toQrtzAnimeList(): List<ProviderAnime> = buildList {
        for (i in 0 until length()) optJSONObject(i)?.toQrtzAnime()?.let(::add)
    }

    private fun JSONObject.toQrtzAnime(fallbackSlug: String? = null): ProviderAnime? {
        val title = optString("title").trim().ifBlank { return null }
        val slug = optString("slug").trim().ifBlank { fallbackSlug ?: return null }
        val genres = optJSONArray("genres").toStringList()
        return ProviderAnime(
            id = "$id:$slug", title = title, providerId = id,
            posterUrl = optString("thumbnail").ifBlank { optString("thumb").ifBlank { null } },
            genres = genres,
            status = optString("status").ifBlank { "UNKNOWN" },
            rating = optString("score").toDoubleOrNull()
        )
    }

    private fun JSONArray.toProviderEpisodes(slug: String): List<ProviderEpisode> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val endpoint = item.optString("endpoint").trim()
            val number = extractEpisodeNumber(item.optString("title"), endpoint) ?: continue
            add(ProviderEpisode(
                id = "$id:${endpoint.ifBlank { "$slug-episode-$number" }}",
                animeId = "$id:$slug", number = number, providerId = id,
                title = item.optString("title").ifBlank { "Episode $number" }
            ))
        }
    }.distinctBy { it.number }.sortedBy { it.number }

    private fun JSONArray.toQrtzEpisodes(slug: String): List<ProviderEpisode> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val endpoint = item.optString("slug").trim()
            val number = item.optString("episode").toIntOrNull()
                ?: extractEpisodeNumber(item.optString("title"), endpoint)
                ?: continue
            add(ProviderEpisode(
                id = "$id:${endpoint.ifBlank { "$slug-episode-$number" }}",
                animeId = "$id:$slug", number = number, providerId = id,
                title = item.optString("title").ifBlank { "Episode $number" }
            ))
        }
    }.distinctBy { it.number }.sortedBy { it.number }

    private fun streamFromUrl(url: String, quality: String? = null) = ProviderStream(
        providerId = id, url = url, quality = quality?.ifBlank { null },
        language = "Japanese", subtitleLanguage = "Indonesian",
        type = when {
            url.contains(".m3u8", true) -> StreamType.HLS
            url.contains(".mpd", true) -> StreamType.DASH
            url.contains(".mp4", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    )

    private fun extractEpisodeNumber(title: String, endpoint: String): Int? {
        val value = "$title $endpoint"
        return Regex("(?:episode|eps)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("(?:^|-)e?(\\d+)(?:-|$)", RegexOption.IGNORE_CASE).find(endpoint)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractYear(value: String): Int? = Regex("\\b(19\\d{2}|20\\d{2})\\b").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun normalizeSlug(value: String): String {
        val raw = value.trim()
        if (!raw.startsWith("http", true)) return raw.trim('/')
        return runCatching {
            URI(raw).path.substringAfter("/anime/", "").trim('/').ifBlank { raw.substringAfterLast('/').trim('/') }
        }.getOrDefault(raw.substringAfterLast('/').trim('/'))
    }

    private fun isUsableSlug(slug: String): Boolean =
        slug.isNotBlank() && !slug.startsWith("1piece", true) && slug.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*"))

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (!has(key) || isNull(key)) null else optString(key).toDoubleOrNull() ?: optDouble(key, Double.NaN).takeUnless { it.isNaN() }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList { for (i in 0 until length()) { val value = optString(i).trim(); if (value.isNotBlank()) add(value) } }
    }

    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.trim('/').split('/').joinToString("/") { encode(it) }
}
