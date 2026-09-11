package com.kakaanime.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

/** Runtime anime airing schedule backed by AniList's public GraphQL endpoint. */
data class AniListScheduleEntry(
    val id: Int,
    val title: String,
    val episode: Int,
    val airingAt: Long,
    val imageUrl: String?,
    val siteUrl: String?
)

class AniListCalendarService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun getSchedule(
        from: Long = Instant.now().epochSecond,
        days: Int = 14
    ): List<AniListScheduleEntry> = withContext(Dispatchers.IO) {
        val to = from + days.coerceIn(1, 30) * 24L * 60L * 60L
        val query = """
            query AiringSchedule(${'$'}from: Int!, ${'$'}to: Int!) {
              Page(perPage: 50) {
                airingSchedules(airingAt_greater: ${'$'}from, airingAt_lesser: ${'$'}to) {
                  id
                  episode
                  airingAt
                  media {
                    id
                    siteUrl
                    title { romaji english native }
                    coverImage { large medium }
                  }
                }
              }
            }
        """.trimIndent()

        runCatching {
            val payload = JSONObject()
                .put("query", query)
                .put("variables", JSONObject().put("from", from.toInt()).put("to", to.toInt()))

            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .header("User-Agent", "KakaAnime/0.1")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching emptyList()
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return@runCatching emptyList()
                parse(body)
            }
        }.getOrElse { emptyList() }
    }

    private fun parse(body: String): List<AniListScheduleEntry> {
        val root = JSONObject(body)
        val schedules = root.optJSONObject("data")
            ?.optJSONObject("Page")
            ?.optJSONArray("airingSchedules")
            ?: return emptyList()

        return buildList {
            for (i in 0 until schedules.length()) {
                val item = schedules.optJSONObject(i) ?: continue
                val media = item.optJSONObject("media") ?: continue
                val title = media.optJSONObject("title") ?: continue
                val displayTitle = sequenceOf("english", "romaji", "native")
                    .mapNotNull { title.optString(it).trim().takeIf(String::isNotBlank) }
                    .firstOrNull() ?: continue
                val episode = item.optInt("episode", 0)
                val airingAt = item.optLong("airingAt", 0L)
                if (episode <= 0 || airingAt <= 0L) continue

                val cover = media.optJSONObject("coverImage")
                add(
                    AniListScheduleEntry(
                        id = media.optInt("id", 0),
                        title = displayTitle,
                        episode = episode,
                        airingAt = airingAt,
                        imageUrl = cover?.optString("large")?.takeIf { it.isNotBlank() }
                            ?: cover?.optString("medium")?.takeIf { it.isNotBlank() },
                        siteUrl = media.optString("siteUrl").takeIf { it.isNotBlank() }
                    )
                )
            }
        }.sortedBy { it.airingAt }
    }
}
