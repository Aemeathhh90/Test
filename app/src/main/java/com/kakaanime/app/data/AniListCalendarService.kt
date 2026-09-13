package com.kakaanime.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Runtime anime airing schedule backed by AniList's public GraphQL endpoint. */
data class AniListScheduleEntry(
    val id: Int,
    val title: String,
    val episode: Int,
    val airingAt: Long,
    val imageUrl: String?,
    val siteUrl: String?,
    val format: String?,
    val score: Double?,
    val durationMinutes: Int?,
    val genres: List<String>
)

class AniListCalendarService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun getSchedule(
        from: Long = startOfTodayEpochSecond(),
        days: Int = 14
    ): List<AniListScheduleEntry> = withContext(Dispatchers.IO) {
        val to = from + days.coerceIn(1, 30) * 24L * 60L * 60L
        val query = """
            query AiringSchedule(${'$'}page: Int!, ${'$'}from: Int!, ${'$'}to: Int!) {
              Page(page: ${'$'}page, perPage: 50) {
                pageInfo { hasNextPage }
                airingSchedules(airingAt_greater: ${'$'}from, airingAt_lesser: ${'$'}to) {
                  id
                  episode
                  airingAt
                  media {
                    id
                    siteUrl
                    title { romaji english native }
                    coverImage { large medium }
                    format
                    averageScore
                    duration
                    genres
                  }
                }
              }
            }
        """.trimIndent()

        buildList {
            var page = 1
            var hasNextPage = true

            while (hasNextPage && page <= 5) {
                val pageEntries = runCatching {
                    val payload = JSONObject()
                        .put("query", query)
                        .put(
                            "variables",
                            JSONObject()
                                .put("page", page)
                                .put("from", from.toInt())
                                .put("to", to.toInt())
                        )

                    val request = Request.Builder()
                        .url("https://graphql.anilist.co")
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .header("Accept", "application/json")
                        .header("User-Agent", "KakaAnime/0.1")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@runCatching emptyList<AniListScheduleEntry>() to false
                        val body = response.body?.string().orEmpty()
                        if (body.isBlank()) return@runCatching emptyList<AniListScheduleEntry>() to false
                        parse(body)
                    }
                }.getOrElse { emptyList<AniListScheduleEntry>() to false }

                addAll(pageEntries.first)
                hasNextPage = pageEntries.second
                if (pageEntries.first.isEmpty() && !hasNextPage) break
                page++
            }
        }.distinctBy { "${it.id}-${it.episode}-${it.airingAt}" }
            .sortedBy { it.airingAt }
    }

    private fun parse(body: String): Pair<List<AniListScheduleEntry>, Boolean> {
        val root = JSONObject(body)
        val page = root.optJSONObject("data")?.optJSONObject("Page")
            ?: return emptyList<AniListScheduleEntry>() to false
        val schedules = page.optJSONArray("airingSchedules")
            ?: return emptyList<AniListScheduleEntry>() to false
        val hasNextPage = page.optJSONObject("pageInfo")?.optBoolean("hasNextPage", false) ?: false

        val entries = buildList {
            for (i in 0 until schedules.length()) {
                val item = schedules.optJSONObject(i) ?: continue
                val media = item.optJSONObject("media") ?: continue
                val title = media.optJSONObject("title") ?: continue
                val rawTitle = sequenceOf("english", "romaji", "native")
                    .mapNotNull { title.optString(it).trim().takeIf(String::isNotBlank) }
                    .firstOrNull() ?: continue
                val mediaId = media.optInt("id", 0)
                val displayTitle = canonicalAppTitle(mediaId, rawTitle)
                val episode = item.optInt("episode", 0)
                val airingAt = item.optLong("airingAt", 0L)
                if (episode <= 0 || airingAt <= 0L) continue

                val cover = media.optJSONObject("coverImage")
                val genres = buildList {
                    val values = media.optJSONArray("genres") ?: return@buildList
                    for (index in 0 until values.length()) {
                        values.optString(index).trim().takeIf { it.isNotBlank() }?.let(::add)
                    }
                }

                add(
                    AniListScheduleEntry(
                        id = mediaId,
                        title = displayTitle,
                        episode = episode,
                        airingAt = airingAt,
                        imageUrl = cover?.optString("large")?.takeIf { it.isNotBlank() }
                            ?: cover?.optString("medium")?.takeIf { it.isNotBlank() },
                        siteUrl = media.optString("siteUrl").takeIf { it.isNotBlank() },
                        format = media.optString("format").takeIf { it.isNotBlank() },
                        score = media.optDouble("averageScore", Double.NaN).takeIf { !it.isNaN() },
                        durationMinutes = media.optInt("duration", 0).takeIf { it > 0 },
                        genres = genres
                    )
                )
            }
        }
        return entries to hasNextPage
    }

    /**
     * Keeps AniList's media identity while normalizing known app entries to the
     * titles used by the current KakaAnime detail catalog. This prevents a tap
     * on a schedule card from becoming a dead end when AniList uses a season
     * subtitle or alternate capitalization.
     */
    private fun canonicalAppTitle(mediaId: Int, fallbackTitle: String): String = when (mediaId) {
        21 -> "One Piece"
        176496 -> "Solo Leveling"
        else -> fallbackTitle
    }

    private fun startOfTodayEpochSecond(): Long =
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond()
}
