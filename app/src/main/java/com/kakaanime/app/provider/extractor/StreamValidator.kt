package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Lightweight playback preflight.
 *
 * Extension-less/signed URLs are classified from the actual HTTP response
 * before Media3 sees them. UNKNOWN is never considered valid by itself:
 * a partial-content response must still prove that it is a supported format.
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
                .header("Accept", "application/vnd.apple.mpegurl, application/dash+xml, video/*, */*")
                .apply {
                    stream.headers.forEach { (key, value) -> header(key, value) }
                    if (stream.type == StreamType.MP4) {
                        header("Range", "bytes=0-4095")
                    }
                }
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null

                val finalUrl = response.request.url.toString()
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                val probeBytes = response.peekBody(65_536).bytes()
                val probeText = probeBytes.toString(StandardCharsets.UTF_8)

                val detectedType = stream.type.takeIf { it != StreamType.UNKNOWN }
                    ?: detectType(finalUrl, contentType, probeText, probeBytes)

                // HTTP 206 only proves that a byte range was served. It is not
                // enough to classify an UNKNOWN stream as playable media.
                val valid = when (detectedType) {
                    StreamType.HLS -> probeText.contains("#EXTM3U", ignoreCase = true) ||
                        contentType.contains("mpegurl") || contentType.contains("m3u8")
                    StreamType.DASH -> probeText.contains("<MPD", ignoreCase = true) ||
                        contentType.contains("dash") || contentType.contains("mpd")
                    StreamType.MP4 -> contentType.contains("video") ||
                        contentType.contains("octet-stream") || hasMp4Signature(probeBytes) ||
                        response.code == 206 && stream.type == StreamType.MP4
                    StreamType.UNKNOWN -> false
                }

                if (!valid) return@runCatching null

                stream.copy(url = finalUrl, type = detectedType)
            }
        }.getOrNull()
    }

    private fun detectType(
        url: String,
        contentType: String,
        probeText: String,
        probeBytes: ByteArray
    ): StreamType {
        val clean = url.substringBefore('?').substringBefore('#').lowercase()
        return when {
            clean.endsWith(".m3u8") ||
                contentType.contains("mpegurl") ||
                contentType.contains("m3u8") ||
                probeText.contains("#EXTM3U") -> StreamType.HLS
            clean.endsWith(".mpd") ||
                contentType.contains("dash") ||
                contentType.contains("mpd") ||
                probeText.contains("<MPD", ignoreCase = true) -> StreamType.DASH
            contentType.contains("video/mp4") ||
                clean.endsWith(".mp4") ||
                hasMp4Signature(probeBytes) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    }

    private fun hasMp4Signature(bytes: ByteArray): Boolean =
        bytes.size >= 8 &&
            bytes.copyOfRange(4, 8).toString(StandardCharsets.US_ASCII) == "ftyp"

    private companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
