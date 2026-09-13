package com.kakaanime.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AniListMetadata(val id: Int, val imageUrl: String?, val bannerUrl: String?)

class AniListMetadataService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).callTimeout(15, TimeUnit.SECONDS).build()

    suspend fun findByTitle(title: String): AniListMetadata? = withContext(Dispatchers.IO) {
        val query = "query(${'$'}s:String!){Media(search:${'$'}s,type:ANIME){id coverImage{extraLarge large medium} bannerImage}}"
        runCatching {
            val body = JSONObject().put("query", query).put("variables", JSONObject().put("s", title)).toString()
            val request = Request.Builder().url("https://graphql.anilist.co")
                .post(body.toRequestBody("application/json".toMediaType())).header("Accept", "application/json").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val media = JSONObject(response.body?.string().orEmpty()).optJSONObject("data")?.optJSONObject("Media") ?: return@runCatching null
                val cover = media.optJSONObject("coverImage")
                AniListMetadata(
                    id = media.optInt("id", 0),
                    imageUrl = cover?.optString("extraLarge")?.takeIf { it.isNotBlank() } ?: cover?.optString("large")?.takeIf { it.isNotBlank() } ?: cover?.optString("medium")?.takeIf { it.isNotBlank() },
                    bannerUrl = media.optString("bannerImage").takeIf { it.isNotBlank() }
                )
            }
        }.getOrNull()
    }
}
