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
 * First real KakaAnime provider adapter.
 *
 * The upstream API is an unofficial Otakudesu wrapper. Keeping the wrapper
 * behind this adapter means the rest of KakaAnime remains provider-agnostic.
 */
class OtakudesuProvider : AnimeProvider {

    override val id = "otakudesu"
    override val name = "Otakudesu"
    override val priority = 10

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://otakudesu-api-jade.vercel.app/api"

    override suspend fun search(query: String): List<ProviderAnime> =
        requestJson("$baseUrl/search/${encode(query)}")
            ?.optJSONArray("search_results")
            ?.toProviderAnimeList()
            ?: emptyList()

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:")
        val root = requestJson("$baseUrl/anime/${encodePath(slug)}") ?: return null
        val detail = root.optJSONObject("anime_detail") ?: return null
        return detail.toProviderAnime(slug)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:")
        val root = requestJson("$baseUrl/anime/${encodePath(slug)}") ?: return emptyList()
        val detail = root.optJSONObject("anime_detail") ?: return emptyList()
        val episodes = detail.optJSONArray("episode_list") ?: return emptyList()

        return buildList {
            for (index in 0 until episodes.length()) {
                val item = episodes.optJSONObject(index) ?: continue
                val endpoint = item.optString("endpoint").trim()
                val number = extractEpisodeNumber(
                    item.optString("title"),
                    endpoint
                ) ?: continue
                add(
                    ProviderEpisode(
                        id = "$id:${endpoint.ifBlank { "$slug-episode-$number" }}",
                        animeId = "$id:$slug",
                        number = number,
                        providerId = id,
                        title = item.optString("title").ifBlank { "Episode $number" }
                    )
                )
            }
        }.sortedBy { it.number }
    }

    override suspend fun getStreams(
        animeId: String,
        episodeNumber: Int
    ): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val endpoint = episode.id.removePrefix("$id:")
        val root = requestJson("$baseUrl/episode/${encodePath(endpoint)}") ?: return emptyList()
        val detail = root.optJSONObject("episode_detail") ?: return emptyList()

        val streams = mutableListOf<ProviderStream>()
        val direct = detail.optString("stream_link").trim()
        if (direct.isNotBlank()) {
            streams += streamFromUrl(direct)
        }

        val downloads = detail.optJSONArray("downloads") ?: JSONArray()
        for (i in 0 until downloads.length()) {
            val qualityGroup = downloads.optJSONObject(i) ?: continue
            val quality = qualityGroup.optString("quality").trim()
            val links = qualityGroup.optJSONArray("links") ?: continue
            for (j in 0 until links.length()) {
                val link = links.optJSONObject(j)?.optString("url")?.trim().orEmpty()
                if (link.isNotBlank()) {
                    streams += streamFromUrl(link, quality)
                }
            }
        }

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

    private fun JSONObject.toProviderAnime(fallbackId: String): ProviderAnime {
        val title = optString("title").ifBlank { "Unknown Anime" }
        val episodeList = optJSONArray("episode_list")
        return ProviderAnime(
            id = "$id:$fallbackId",
            title = title,
            providerId = id,
            posterUrl = optString("thumb").ifBlank { null },
            description = optString("synopsis"),
            year = extractYear(optString("release")),
            status = optString("status").ifBlank { "UNKNOWN" },
            rating = optDoubleOrNull("skor"),
            latestEpisode = episodeList?.let { array ->
                (0 until array.length())
                    .mapNotNull { index ->
                        val item = array.optJSONObject(index) ?: return@mapNotNull null
                        extractEpisodeNumber(item.optString("title"), item.optString("endpoint"))
                    }
                    .maxOrNull()
            }
        )
    }

    private fun JSONArray.toProviderAnimeList(): List<ProviderAnime> = buildList {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            val slug = item.optString("endpoint").trim().trim('/')
            if (slug.isBlank()) continue
            add(
                ProviderAnime(
                    id = "$id:$slug",
                    title = item.optString("title").ifBlank { "Unknown Anime" },
                    providerId = id,
                    posterUrl = item.optString("thumb").ifBlank { null },
                    genres = item.optJSONArray("genres").toStringList(),
                    status = item.optString("status").ifBlank { "UNKNOWN" },
                    rating = item.optDoubleOrNull("rating")
                )
            )
        }
    }

    private fun streamFromUrl(url: String, quality: String? = null): ProviderStream =
        ProviderStream(
            providerId = id,
            url = url,
            quality = quality?.ifBlank { null },
            language = "Japanese",
            subtitleLanguage = "Indonesian",
            type = when {
                url.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
                url.contains(".mpd", ignoreCase = true) -> StreamType.DASH
                else -> StreamType.UNKNOWN
            }
        )

    private fun extractEpisodeNumber(title: String, endpoint: String): Int? {
        val value = "$title $endpoint"
        return Regex("(?:episode|eps)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE)
            .find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("(?:^|-)e?(\\d+)(?:-|$)", RegexOption.IGNORE_CASE)
                .find(endpoint)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractYear(value: String): Int? =
        Regex("\\b(19\\d{2}|20\\d{2})\\b").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (!has(key) || isNull(key)) null else optString(key).toDoubleOrNull() ?: optDouble(key, Double.NaN).takeUnless { it.isNaN() }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val value = optString(i).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")

    private fun encodePath(value: String): String =
        value.trim('/').split('/').joinToString("/") { encode(it) }
}
