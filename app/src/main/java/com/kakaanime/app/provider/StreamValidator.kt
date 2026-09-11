package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Runtime gate between provider discovery and the player.
 *
 * A URL is not considered playable just because it looks like .m3u8/.mpd/.mp4.
 * The validator performs a small network probe and rejects HTML/error pages,
 * redirects that land on pages, and dead streams before they reach Media3.
 */
class StreamValidator(
    private val client: OkHttpClient = defaultClient()
) {
    suspend fun validate(streams: List<NormalizedStream>): List<NormalizedStream> = coroutineScope {
        streams
            .take(MAX_CANDIDATES)
            .map { stream ->
                async(Dispatchers.IO) {
                    if (isPlayable(stream)) stream else null
                }
            }
            .awaitAll()
            .filterNotNull()
    }

    private fun isPlayable(stream: NormalizedStream): Boolean {
        val url = stream.url.trim()
        if (!url.startsWith("https://", ignoreCase = true) &&
            !url.startsWith("http://", ignoreCase = true)) return false

        return runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .apply {
                    stream.headers.forEach { (key, value) -> header(key, value) }
                    if (stream.type == StreamType.MP4) header("Range", "bytes=0-1023")
                }
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body ?: return false
                val contentType = body.contentType()?.toString()?.lowercase().orEmpty()

                when (stream.type) {
                    StreamType.HLS -> {
                        val sample = body.source().buffer.peek().readUtf8(8192)
                        sample.contains("#EXTM3U", ignoreCase = true)
                    }
                    StreamType.DASH -> {
                        val sample = body.source().buffer.peek().readUtf8(8192)
                        sample.contains("<MPD", ignoreCase = true) ||
                            contentType.contains("dash") ||
                            contentType.contains("mpd")
                    }
                    StreamType.MP4 -> {
                        response.code == 206 ||
                            contentType.contains("video/mp4") ||
                            contentType.contains("application/mp4")
                    }
                    StreamType.UNKNOWN -> false
                }
            }
        }.getOrDefault(false)
    }

    companion object {
        private const val MAX_CANDIDATES = 8

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(7, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }
}
