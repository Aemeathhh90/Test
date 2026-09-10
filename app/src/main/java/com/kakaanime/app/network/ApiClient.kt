package com.kakaanime.app.network

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

object ApiClient {

    private const val BASE_URL =
        "https://didatic-computing-machine-g49vrx9jjjp4hww9v-3000.app.github.dev"

    private val client = OkHttpClient()

    fun getAnimeList(): JSONArray? {
        val request = Request.Builder()
            .url("$BASE_URL/api/anime")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val body = response.body?.string()
                    ?: return null

                JSONArray(body)
            }
        } catch (e: Exception) {
            null
        }
    }
}
