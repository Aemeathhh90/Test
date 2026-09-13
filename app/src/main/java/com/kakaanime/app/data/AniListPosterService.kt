package com.kakaanime.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AniListPosterService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun getPosterUrls(titles: Collection<String>): Map<String, String> = withContext(Dispatchers.IO) {
        titles.distinct().associateWith { title -> queryPoster(title) }.filterValues { !it.isNullOrBlank() }.mapValues { it.value!! }
    }

    private fun queryPoster(title: String): String? {
        val query = """
            query MediaByTitle(${ '$' }search: String!) {
              Media(search: ${ '$' }search, type: ANIME) {
                title { romaji english native }
                coverImage { extraLarge large medium }
              }
            }
        """.trimIndent()
        return runCatching {
            val payload = JSONObject()
                .put("query", query)
                .put("variables", JSONObject().put("search", title))
            val request = Request.Builder()
                .url("https://graphql.anilist.co")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .header("Accept", "application/json")
                .header("User-Agent", "KakaAnime/0.1")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val media = JSONObject(response.body?.string().orEmpty())
                    .optJSONObject("data")?.optJSONObject("Media") ?: return@runCatching null
                val cover = media.optJSONObject("coverImage") ?: return@runCatching null
                sequenceOf("extraLarge", "large", "medium")
                    .mapNotNull { cover.optString(it).trim().takeIf(String::isNotBlank) }
                    .firstOrNull()
            }
        }.getOrNull()
    }
}
