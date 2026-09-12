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
    private val legacySearchUrl = "https://otakudesu-api-jade.vercel.app/api"

    override suspend fun search(query: String): List<ProviderAnime> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        val primary = requestJson(
            "$baseUrl/search?q=${encode(normalizedQuery)}&page=1"
        )
        val primaryResults = primary
            ?.optJSONObject("data")
            ?.optJSONArray("results")
            ?.toProviderAnimeList()
            .orEmpty()

        if (primaryResults.isNotEmpty()) return primaryResults

        val fallback = requestJson(
            "$legacySearchUrl/search/${encodePath(normalizedQuery)}"
        )
        return fallback?.optJSONArray("search_results")
            ?.toLegacyProviderAnimeList()
            .orEmpty()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = normalizeAnimeSlug(animeId)

        val primary = requestData("$baseUrl/anime/${encodePath(slug)}")
        if (primary != null) return primary.toProviderAnime(slug)

        return requestLegacyAnime(slug)?.toLegacyProviderAnime(slug)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeAnimeSlug(animeId)

        val primary = requestData("$baseUrl/anime/${encodePath(slug)}")
        val primaryEpisodes = primary?.optJSONArray("episodeList")
            ?.toProviderEpisodeList(slug)
            .orEmpty()
        if (primaryEpisodes.isNotEmpty()) return primaryEpisodes

        val legacyDetail = requestLegacyAnime(slug) ?: return emptyList()
        val animeDetail = legacyDetail.optJSONObject("anime_detail") ?: legacyDetail
        return animeDetail.optJSONArray("episode_list")
            ?.toLegacyProviderEpisodeList(slug)
            .orEmpty()
    }

    override suspend fun getStreams(
        animeId: String,
        episodeNumber: Int
    ): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val episodeSlug = episode.id.removePrefix("$id:").trim('/')

        val primary = requestData("$baseUrl/episode/${encodePath(episodeSlug)}")
        if (primary != null) {
            val streams = primary.toQrtzStreams()
            if (streams.isNotEmpty()) return streams
        }

        val legacy = requestLegacyEpisode(episodeSlug) ?: return emptyList()
        val detail = legacy.optJSONObject("episode_detail") ?: legacy
        val streamUrl = detail.optString("stream_link").trim()
        if (streamUrl.isBlank()) return emptyList()

        return listOf(streamFromUrl(streamUrl))
    }

    private suspend fun requestData(url: String): JSONObject? =
        requestJson(url)?.optJSONObject("data")

    private suspend fun requestLegacyAnime(slug: String): JSONObject? =
        requestJson("$legacySearchUrl/anime/${encodePath(slug)}")

    private suspend fun requestLegacyEpisode(slug: String): JSONObject? =
        requestJson("$legacySearchUrl/episode/${encodePath(slug)}")

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

    private fun JSONObject.toLegacyProviderAnime(fallbackId: String): ProviderAnime {
        val detail = optJSONObject("anime_detail") ?: this
        val title = detail.optString("title").ifBlank { "Unknown Anime" }
        val synopsis = detail.optString("synopsis")
        val score = detail.optString("skor").ifBlank { detail.optString("score") }
        val status = detail.optString("status").ifBlank { "UNKNOWN" }
        val poster = detail.optString("thumb").ifBlank { detail.optString("thumbnail") }
        val episodes = detail.optJSONArray("episode_list")

        return ProviderAnime(
            id = "$id:$fallbackId",
            title = title,
            providerId = id,
            posterUrl = poster.ifBlank { null },
            description = synopsis,
            status = status,
            rating = score.toDoubleOrNull(),
            latestEpisode = episodes?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    extractEpisodeNumber(
                        item.optString("title"),
                        item.optString("endpoint")
                    )
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

    private fun JSONArray.toLegacyProviderAnimeList(): List<ProviderAnime> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val slug = normalizeAnimeSlug(item.optString("endpoint"))
            if (slug.isBlank()) continue
            add(
                ProviderAnime(
                    id = "$id:$slug",
                    title = item.optString("title").ifBlank { "Unknown Anime" },
                    providerId = id,
                    posterUrl = item.optString("thumb").ifBlank { null },
                    status = item.optString("status").ifBlank { "UNKNOWN" },
                    rating = item.optString("rating").toDoubleOrNull()
                )
            )
        }
    }

    private fun JSONArray.toProviderEpisodeList(animeSlug: String): List<ProviderEpisode> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val endpoint = item.optString("slug").trim().trim('/')
            val number = item.optString("episode").toIntOrNull()
                ?: extractEpisodeNumber(item.optString("title"), endpoint)
                ?: continue

            add(
                ProviderEpisode(
                    id = "$id:${endpoint.ifBlank { "$animeSlug-episode-$number-sub-indo" }}",
                    animeId = "$id:$animeSlug",
                    number = number,
                    providerId = id,
                    title = item.optString("title").ifBlank { "Episode $number" }
                )
            )
        }
    }.sortedBy { it.number }

    private fun JSONArray.toLegacyProviderEpisodeList(animeSlug: String): List<ProviderEpisode> = buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val endpoint = normalizeEpisodeSlug(item.optString("endpoint"))
            val number = extractEpisodeNumber(item.optString("title"), endpoint) ?: continue

            add(
                ProviderEpisode(
                    id = "$id:$endpoint",
                    animeId = "$id:$animeSlug",
                    number = number,
                    providerId = id,
                    title = item.optString("title").ifBlank { "Episode $number" }
                )
            )
        }
    }.sortedBy { it.number }

    private fun JSONObject.toQrtzStreams(): List<ProviderStream> {
        val streams = mutableListOf<ProviderStream>()
        val streamArray = optJSONArray("streams") ?: JSONArray()
        for (index in 0 until streamArray.length()) {
            val item = streamArray.optJSONObject(index) ?: continue
            val url = item.optString("embedUrl").trim()
            if (url.isBlank()) continue
            streams += streamFromUrl(
                url = url,
                quality = item.optString("resolution").ifBlank { null }
            )
        }

        val defaultStream = optString("defaultStreamUrl").trim()
        if (defaultStream.isNotBlank() && streams.none { it.url == defaultStream }) {
            streams += streamFromUrl(defaultStream)
        }

        return streams.distinctBy { it.url }
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

    private fun normalizeAnimeSlug(value: String): String {
        val raw = value.removePrefix("$id:").trim()
        return raw.substringAfter("/anime/", raw)
            .substringBefore("?")
            .trim('/')
    }

    private fun normalizeEpisodeSlug(value: String): String {
        val raw = value.removePrefix("$id:").trim()
        return raw.substringAfter("/episode/", raw)
            .substringBefore("?")
            .trim('/')
    }

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
