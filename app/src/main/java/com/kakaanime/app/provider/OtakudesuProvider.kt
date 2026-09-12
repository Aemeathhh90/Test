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
 * Otakudesu provider adapter.
 *
 * The previous jade wrapper is no longer returning the detail payload needed
 * by the provider. This adapter uses the current qrtzanim Otakudesu REST
 * wrapper and keeps the rest of AniLab provider-agnostic.
 */
class OtakudesuProvider : AnimeProvider {

    override val id = "otakudesu"
    override val name = "Otakudesu"
    override val priority = 10

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://qrtzanim.vercel.app/api"

    override suspend fun search(query: String): List<ProviderAnime> {
        val root = requestJson("$baseUrl/search?q=${encode(query)}&page=1") ?: return emptyList()
        return root.optJSONObject("data")
            ?.optJSONArray("results")
            ?.toProviderAnimeList()
            ?: emptyList()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:")
        val data = requestData("$baseUrl/anime/${encodePath(slug)}") ?: return null
        return data.toProviderAnime(slug)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:")
        val data = requestData("$baseUrl/anime/${encodePath(slug)}") ?: return emptyList()
        val episodes = data.optJSONArray("episodeList") ?: return emptyList()

        return buildList {
            for (index in 0 until episodes.length()) {
                val item = episodes.optJSONObject(index) ?: continue
                val endpoint = item.optString("slug").trim()
                val number = item.optString("episode").toIntOrNull()
                    ?: extractEpisodeNumber(item.optString("title"), endpoint)
                    ?: continue

                add(
                    ProviderEpisode(
                        id = "$id:${endpoint.ifBlank { "$slug-episode-$number-sub-indo" }}",
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
        val slug = episode.id.removePrefix("$id:")
        val data = requestData("$baseUrl/episode/${encodePath(slug)}") ?: return emptyList()

        val streams = mutableListOf<ProviderStream>()
        val streamArray = data.optJSONArray("streams") ?: JSONArray()
        for (index in 0 until streamArray.length()) {
            val item = streamArray.optJSONObject(index) ?: continue
            val url = item.optString("embedUrl").trim()
            if (url.isBlank()) continue
            streams += streamFromUrl(
                url = url,
                quality = item.optString("resolution").ifBlank { null }
            )
        }

        val defaultStream = data.optString("defaultStreamUrl").trim()
        if (defaultStream.isNotBlank() && streams.none { it.url == defaultStream }) {
            streams += streamFromUrl(defaultStream)
        }

        return streams.distinctBy { it.url }
    }

    private suspend fun requestData(url: String): JSONObject? =
        requestJson(url)?.optJSONObject("data")

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
        val metadata = optJSONObject("metadata")
        val title = optString("title").ifBlank {
            metadata?.optString("judul").orEmpty()
        }.ifBlank { "Unknown Anime" }
        val episodeList = optJSONArray("episodeList")
        val synopsis = optJSONArray("synopsis")?.toStringList()?.joinToString("\n")
            ?: optString("synopsis")

        return ProviderAnime(
            id = "$id:$fallbackId",
            title = title,
            providerId = id,
            posterUrl = optString("thumbnail").ifBlank { null },
            description = synopsis,
            year = extractYear(metadata?.optString("releaseTime").orEmpty()),
            status = metadata?.optString("status").ifNullOrBlank("UNKNOWN"),
            rating = metadata?.optString("score")?.toDoubleOrNull()
                ?: optString("score").toDoubleOrNull(),
            latestEpisode = episodeList?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    item.optString("episode").toIntOrNull()
                        ?: extractEpisodeNumber(item.optString("title"), item.optString("slug"))
                }.maxOrNull()
            }
        )
    }

    private fun JSONArray.toProviderAnimeList(): List<ProviderAnime> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val slug = item.optString("slug").trim().trim('/')
            if (slug.isBlank()) continue
            add(
                ProviderAnime(
                    id = "$id:$slug",
                    title = item.optString("title").ifBlank { "Unknown Anime" },
                    providerId = id,
                    posterUrl = item.optString("thumbnail").ifBlank { null },
                    status = item.optString("status").ifBlank { "UNKNOWN" },
                    rating = item.optString("score").toDoubleOrNull()
                )
            )
        }
    }

    private fun streamFromUrl(url: String, quality: String? = null): ProviderStream =
        ProviderStream(
            providerId = id,
            url = url,
            quality = quality,
            language = "Japanese",
            subtitleLanguage = "Indonesian",
            type = when {
                url.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
                url.contains(".mpd", ignoreCase = true) -> StreamType.DASH
                url.contains(".mp4", ignoreCase = true) -> StreamType.MP4
                else -> StreamType.UNKNOWN
            }
        )

    private fun extractEpisodeNumber(title: String, slug: String): Int? {
        val value = "$title $slug"
        return Regex("(?:episode|eps)[^0-9]*(\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
            .find(value)?.groupValues?.getOrNull(1)?.toDoubleOrNull()?.toInt()
            ?: Regex("(?:^|-)e?(\\d+)(?:-|$)", RegexOption.IGNORE_CASE)
                .find(slug)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractYear(value: String): Int? =
        Regex("\\b(19\\d{2}|20\\d{2})\\b").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun String?.ifNullOrBlank(fallback: String): String =
        if (this.isNullOrBlank()) fallback else this

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val value = optString(index).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")

    private fun encodePath(value: String): String =
        value.trim('/').split('/').joinToString("/") { encode(it) }
}
