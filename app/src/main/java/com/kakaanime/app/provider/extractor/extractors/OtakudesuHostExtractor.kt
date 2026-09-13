package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

/** Host-level fallback for OtakuDesu mirror embeds, aligned with CloudStream host routing. */
class OtakudesuHostExtractor : StreamExtractor {
    override val id = "otakudesu-host"
    override val priority = 115

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun canHandle(url: String): Boolean {
        val host = runCatching { URI(url).host?.lowercase().orEmpty() }.getOrDefault("")
        return host.contains("filedon") || host.contains("uservideo") || host.contains("userdrive") ||
            host.contains("samevideo") || host.contains("vidhide") || host.contains("blogger") ||
            host.contains("blogspot") || host.contains("mp4upload") || host.contains("yourupload") ||
            host.contains("yuplod") || host.contains("streamwish") || host.contains("filelions")
    }

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> = withContext(Dispatchers.IO) {
        val html = get(url, referer) ?: return@withContext emptyList()
        val results = linkedMapOf<String, ProviderStream>()

        // CloudStream's FileDon/UserVideo/UserDrive/SameVideo path reads
        // div#app[data-page] and takes props.url from the embedded JSON.
        Regex("<div[^>]+id=[\\\"']app[\\\"'][^>]+data-page=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.getOrNull(1)?.let { raw ->
                runCatching {
                    val page = JSONObject(decodeHtml(raw))
                    val props = page.optJSONObject("props")
                    val media = props?.optString("url")?.takeIf(String::isNotBlank)
                    if (media != null) addMedia(results, resolveUrl(url, media) ?: media, url)
                }
            }

        // Generic host-page sources are intentionally accepted even when the
        // final URL has no extension; StreamValidator will classify it from
        // response headers/body instead of guessing MP4 here.
        val patterns = listOf(
            "<source[^>]+(?:src|data-src)=[\\\"']([^\\\"']+)",
            "<video[^>]+(?:src|data-src)=[\\\"']([^\\\"']+)",
            "<(?:iframe|embed)[^>]+(?:src|data-src)=[\\\"']([^\\\"']+)"
        )
        patterns.forEach { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(html).forEach { match ->
                val candidate = decodeHtml(match.groupValues[1])
                resolveUrl(url, candidate)?.let { resolved ->
                    if (resolved != url) addMedia(results, resolved, url)
                }
            }
        }

        Regex("https?://[^\\s\\\"'<>]+\\.(?:m3u8|mpd|mp4|mkv|webm)(?:\\?[^\\s\\\"'<>]*)?", RegexOption.IGNORE_CASE)
            .findAll(html).forEach { addMedia(results, it.value, url) }

        results.values.toList()
    }

    private fun addMedia(results: MutableMap<String, ProviderStream>, url: String, referer: String) {
        val clean = url.trim()
        if (!clean.startsWith("http", true)) return
        val type = when {
            clean.substringBefore('?').substringBefore('#').endsWith(".m3u8", true) -> StreamType.HLS
            clean.substringBefore('?').substringBefore('#').endsWith(".mpd", true) -> StreamType.DASH
            clean.substringBefore('?').substringBefore('#").endsWith(".mp4", true) ||
                clean.substringBefore('?').substringBefore('#").endsWith(".mkv", true) ||
                clean.substringBefore('?').substringBefore('#").endsWith(".webm", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
        results.putIfAbsent(clean, ProviderStream("otakudesu", clean, type = type, headers = mapOf("User-Agent" to UA, "Referer" to referer)))
    }

    private suspend fun get(url: String, referer: String?): String? = withContext(Dispatchers.IO) {
        runCatching {
            val builder = Request.Builder().url(url)
                .header("User-Agent", UA)
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8")
            if (!referer.isNullOrBlank()) builder.header("Referer", referer)
            client.newCall(builder.build()).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        }.getOrNull()
    }

    private fun resolveUrl(base: String, candidate: String): String? =
        runCatching { URI(base).resolve(candidate).toString() }.getOrNull()

    private fun decodeHtml(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    private companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
