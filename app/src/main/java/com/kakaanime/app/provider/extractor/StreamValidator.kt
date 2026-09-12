package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Lightweight playback preflight inspired by MaxStream/OCE.
 *
 * We do not assume that an extracted URL is playable merely because it looks
 * like media. A small request verifies that the candidate is reachable and,
 * for manifests, that it actually contains the expected playlist structure.
 */
class StreamValidator {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun validate(stream: ProviderStream): ProviderStream? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(stream.url)
                .header("User-Agent", UA)
                .apply {
                    stream.headers.forEach { (key, value) -> header(key, value) }
                    if (stream.type == StreamType.MP4 || stream.type == StreamType.UNKNOWN) {
                        header("Range", "bytes=0-4095")
                    }
                }
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null

                val finalUrl = response.request.url.toString()
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                val body = when (stream.type) {
                    StreamType.HLS, StreamType.DASH -> response.body?.string().orEmpty().take(32_768)
                    else -> ""
                }

                val valid = when (stream.type) {
                    StreamType.HLS -> body.contains("#EXTM3U", ignoreCase = true)
                    StreamType.DASH -> body.contains("<MPD", ignoreCase = true)
                    StreamType.MP4 -> contentType.contains("video") ||
                        contentType.contains("octet-stream") ||
                        response.code == 206
                    StreamType.UNKNOWN -> contentType.contains("video") ||
                        contentType.contains("mpegurl") ||
                        contentType.contains("dash") ||
                        body.contains("#EXTM3U") ||
                        body.contains("<MPD", ignoreCase = true) ||
                        response.code == 206
                }

                if (!valid) return@runCatching null

                stream.copy(
                    url = finalUrl,
                    type = stream.type.takeIf { it != StreamType.UNKNOWN }
                        ?: detectType(finalUrl, contentType, body)
                )
            }
        }.getOrNull()
    }

    private fun detectType(url: String, contentType: String, body: String): StreamType {
        val clean = url.substringBefore('?').substringBefore('#').lowercase()
        return when {
            clean.endsWith(".m3u8") || contentType.contains("mpegurl") || body.contains("#EXTM3U") -> StreamType.HLS
            clean.endsWith(".mpd") || contentType.contains("dash") || body.contains("<MPD", ignoreCase = true) -> StreamType.DASH
            contentType.contains("video") || clean.endsWith(".mp4") -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    }

    private companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
