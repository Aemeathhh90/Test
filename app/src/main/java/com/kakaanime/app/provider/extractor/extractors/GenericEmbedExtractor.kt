package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Best-effort resolver for embed/player pages.
 *
 * It intentionally stays generic: discover direct media URLs or a small iframe
 * chain, then hand the final media URL back as ProviderStream. Host-specific
 * decryptors can be added later without changing provider code.
 */
class GenericEmbedExtractor : StreamExtractor {
    override val id = "generic-embed"
    override val priority = 20

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    override fun canHandle(url: String): Boolean {
        val value = url.trim()
        if (value.isBlank() || !value.startsWith("http", ignoreCase = true)) return false
        return !value.isDirectMediaUrl()
    }

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> =
        withContext(Dispatchers.IO) {
            resolvePage(url, referer, depth = 0, visited = linkedSetOf())
        }

    private fun resolvePage(
        url: String,
        referer: String?,
        depth: Int,
        visited: MutableSet<String>
    ): List<ProviderStream> {
        if (depth > MAX_DEPTH || !visited.add(url)) return emptyList()

        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
            )
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .apply { if (!referer.isNullOrBlank()) header("Referer", referer) }
            .build()

        val response = runCatching { client.newCall(request).execute() }.getOrNull() ?: return emptyList()
        response.use {
            if (!it.isSuccessful) return emptyList()
            val finalUrl = it.request.url.toString()
            val body = it.body?.string().orEmpty()
            if (body.isBlank()) return emptyList()

            val direct = extractMediaUrls(body)
                .map { mediaUrl ->
                    ProviderStream(
                        providerId = "resolver",
                        url = mediaUrl,
                        type = mediaUrl.toStreamType(),
                        headers = mapOf("Referer" to finalUrl)
                    )
                }
            if (direct.isNotEmpty()) return direct.distinctBy { stream -> stream.url }

            val iframeUrls = extractIframeUrls(body, finalUrl)
            for (iframeUrl in iframeUrls) {
                val nested = resolvePage(iframeUrl, finalUrl, depth + 1, visited)
                if (nested.isNotEmpty()) return nested
            }
        }

        return emptyList()
    }

    private fun extractMediaUrls(html: String): List<String> = buildList {
        val patterns = listOf(
            Regex("<source[^>]+src=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("(?:file|src|source|hls|m3u8|videoUrl|video_url)\\s*[=:]\\s*[\\\"'](https?://[^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("https?://[^\\s\\\"'<>]+\\.(?:m3u8|mpd|mp4|mkv|webm)(?:\\?[^\\s\\\"'<>]*)?", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            for (match in pattern.findAll(html)) {
                val raw = match.groupValues.getOrNull(1)?.ifBlank { match.value }.orEmpty()
                val normalized = decodeHtml(raw).trim()
                if (normalized.startsWith("http", ignoreCase = true) && normalized.isDirectMediaUrl()) {
                    add(normalized)
                }
            }
        }
    }

    private fun extractIframeUrls(html: String, baseUrl: String): List<String> {
        val pattern = Regex(
            "<iframe[^>]+src=[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        )
        return pattern.findAll(html)
            .mapNotNull { match ->
                val raw = decodeHtml(match.groupValues[1].trim())
                resolveUrl(baseUrl, raw)
            }
            .filter { it.startsWith("http", ignoreCase = true) }
            .distinct()
            .toList()
    }

    private fun resolveUrl(baseUrl: String, candidate: String): String? =
        runCatching { URI(baseUrl).resolve(candidate).toString() }.getOrNull()

    private fun decodeHtml(value: String): String =
        value.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

    private fun String.isDirectMediaUrl(): Boolean {
        val clean = substringBefore('?').substringBefore('#').lowercase()
        return clean.endsWith(".m3u8") || clean.endsWith(".mpd") ||
            clean.endsWith(".mp4") || clean.endsWith(".mkv") || clean.endsWith(".webm")
    }

    private fun String.toStreamType(): StreamType {
        val clean = substringBefore('?').substringBefore('#').lowercase()
        return when {
            clean.endsWith(".m3u8") -> StreamType.HLS
            clean.endsWith(".mpd") -> StreamType.DASH
            clean.endsWith(".mp4") || clean.endsWith(".mkv") || clean.endsWith(".webm") -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    }

    private companion object {
        const val MAX_DEPTH = 2
    }
}
