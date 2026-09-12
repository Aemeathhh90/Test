package com.kakaanime.app.provider.extractor.extractors

import android.util.Base64
import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Otakudesu-specific server resolver.
 *
 * This follows the currently referenced CloudStream flow instead of assuming
 * that the episode page contains an iframe directly:
 *
 * episode page -> mirrorstream AJAX/nonce -> iframe -> host extractor
 *                  \-> download servers -> direct/host resolver
 */
class OtakudesuServerExtractor : StreamExtractor {
    override val id = "otakudesu-server"
    override val priority = 100

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(35, TimeUnit.SECONDS)
        .build()

    private val genericEmbed = GenericEmbedExtractor()
    private val kraken = KrakenFilesExtractor()
    private val pixeldrain = PixelDrainExtractor()

    override fun canHandle(url: String): Boolean {
        val value = url.trim().lowercase()
        return value.startsWith("http") &&
            (value.contains("otakudesu.") &&
                (value.contains("/episode/") || value.contains("/episodes/") || value.contains("-episode-")))
    }

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> =
        withContext(Dispatchers.IO) {
            val html = get(url, referer) ?: return@withContext emptyList()
            val results = linkedMapOf<String, ProviderStream>()

            resolveMirrorStream(url, html).forEach { candidate ->
                resolveExternal(candidate, url).forEach { stream -> results.putIfAbsent(stream.url, stream) }
            }

            discoverDownloadLinks(url, html).forEach { candidate ->
                resolveExternal(candidate.url, url, candidate.quality).forEach { stream ->
                    results.putIfAbsent(stream.url, stream)
                }
            }

            // Keep a final generic pass for player/embed markup not covered by
            // Otakudesu's server blocks.
            if (results.isEmpty()) {
                extractInlineMedia(html, url).forEach { media ->
                    results.putIfAbsent(
                        media,
                        ProviderStream(
                            providerId = "otakudesu",
                            url = media,
                            quality = null,
                            type = media.toStreamType(),
                            headers = mapOf("Referer" to url)
                        )
                    )
                }
            }

            results.values.toList()
        }

    private suspend fun resolveMirrorStream(pageUrl: String, html: String): List<String> {
        val host = runCatching { URI(pageUrl).scheme + "://" + URI(pageUrl).authority }.getOrNull()
            ?: return emptyList()
        val script = Regex(
            "<script[^>]*>(.*?)</script>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).findAll(html)
            .map { it.groupValues[1] }
            .filter { it.contains("action:", ignoreCase = true) }
            .lastOrNull()
            ?: return emptyList()

        val actions = Regex(
            "(?:^|[,\\{\\s])action\\s*:\\s*[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        ).findAll(script).map { it.groupValues[1] }.toList()
        if (actions.size < 2) return emptyList()

        val nonce = postAjax(host, mapOf("action" to actions.first())) ?: return emptyList()
        val action = actions[1]

        val mirrorEntries = Regex(
            "(?:data-content|data-video|data-src)\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        ).findAll(html)
            .mapNotNull { decodeBase64(it.groupValues[1]) }
            .flatMap { parseMirrorEntries(it).asSequence() }
            .toList()

        val results = linkedSetOf<String>()
        for (entry in mirrorEntries) {
            val response = postAjax(
                "$host/wp-admin/admin-ajax.php",
                mapOf(
                    "id" to entry.id,
                    "i" to entry.i,
                    "q" to entry.q,
                    "nonce" to nonce,
                    "action" to action
                )
            ) ?: continue

            val decoded = decodeBase64(response) ?: response
            extractIframeUrls(decoded, pageUrl).forEach { results += it }
            extractInlineMedia(decoded, pageUrl).forEach { results += it }
        }
        return results.toList()
    }

    private suspend fun discoverDownloadLinks(pageUrl: String, html: String): List<DownloadCandidate> {
        val result = mutableListOf<DownloadCandidate>()
        val liPattern = Regex(
            "<li[^>]*>(.*?)</li>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        for (li in liPattern.findAll(html)) {
            val block = li.groupValues[1]
            if (!block.contains("href=", ignoreCase = true)) continue
            val quality = Regex("(\\d{3,4})\\s*[pP]").find(block)?.groupValues?.getOrNull(1)?.toIntOrNull()?.toString()
            Regex("href\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
                .findAll(block)
                .forEach { match ->
                    val href = decodeHtml(match.groupValues[1])
                    val absolute = resolveUrl(pageUrl, href) ?: return@forEach
                    result += DownloadCandidate(absolute, quality)
                }
        }
        return result.distinctBy { it.url }
    }

    private suspend fun resolveExternal(
        url: String,
        referer: String,
        quality: String? = null
    ): List<ProviderStream> {
        val clean = resolveRedirect(url, referer) ?: url
        val normalizedQuality = quality ?: Regex("(\\d{3,4})[pP]").find(clean)?.groupValues?.getOrNull(1)

        return when {
            clean.isDirectMediaUrl() -> listOf(
                ProviderStream(
                    providerId = "otakudesu",
                    url = clean,
                    quality = normalizedQuality,
                    type = clean.toStreamType(),
                    headers = mapOf("Referer" to referer)
                )
            )

            clean.contains("pixeldrain.com", ignoreCase = true) ->
                pixeldrain.extract(clean, referer).map { it.copy(quality = it.quality ?: normalizedQuality) }

            clean.contains("krakenfiles.com", ignoreCase = true) ->
                kraken.extract(clean, referer).map { it.copy(quality = it.quality ?: normalizedQuality) }

            else -> genericEmbed.extract(clean, referer).map { stream ->
                stream.copy(
                    providerId = "otakudesu",
                    quality = stream.quality ?: normalizedQuality
                )
            }
        }
    }

    private fun extractIframeUrls(html: String, baseUrl: String): List<String> =
        Regex(
            "<iframe[^>]+(?:src|data-src)\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        ).findAll(html)
            .mapNotNull { resolveUrl(baseUrl, decodeHtml(it.groupValues[1])) }
            .distinct()
            .toList()

    private fun extractInlineMedia(html: String, baseUrl: String): List<String> {
        val urls = linkedSetOf<String>()
        val patterns = listOf(
            Regex("(?:file|src|source|hls|m3u8|videoUrl|video_url|playlist)\\s*[=:]\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("https?://[^\\s\\\"'<>]+\\.(?:m3u8|mpd|mp4|mkv|webm)(?:\\?[^\\s\\\"'<>]*)?", RegexOption.IGNORE_CASE)
        )
        patterns.forEach { pattern ->
            pattern.findAll(html).forEach { match ->
                val raw = match.groupValues.getOrNull(1)?.ifBlank { match.value } ?: return@forEach
                val candidate = resolveUrl(baseUrl, decodeHtml(raw)) ?: return@forEach
                if (candidate.isDirectMediaUrl()) urls += candidate
            }
        }
        return urls.toList()
    }

    private suspend fun resolveRedirect(url: String, referer: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", UA)
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8")
                .header("Referer", referer)
                .build()
            client.newCall(request).execute().use { response ->
                response.request.url.toString()
            }
        }.getOrNull()
    }

    private suspend fun get(url: String, referer: String?): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", UA)
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .apply { if (!referer.isNullOrBlank()) header("Referer", referer) }
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()
            }
        }.getOrNull()
    }

    private suspend fun postAjax(endpoint: String, fields: Map<String, String>): String? = withContext(Dispatchers.IO) {
        runCatching {
            val body = FormBody.Builder().apply { fields.forEach { (key, value) -> add(key, value) } }.build()
            val request = Request.Builder()
                .url(if (endpoint.endsWith("admin-ajax.php")) endpoint else "$endpoint/wp-admin/admin-ajax.php")
                .post(body)
                .header("User-Agent", UA)
                .header("Accept", "application/json,text/plain,*/*")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val raw = response.body?.string().orEmpty()
                if (raw.isBlank()) null else {
                    runCatching { JSONObject(raw).optString("data").ifBlank { raw } }.getOrDefault(raw)
                }
            }
        }.getOrNull()
    }

    private fun parseMirrorEntries(value: String): List<MirrorEntry> {
        val clean = value.trim()
        val jsonArray = runCatching { JSONArray(clean) }.getOrNull() ?: return emptyList()
        return buildList {
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val id = item.optString("id").trim()
                val mirror = item.optString("i").trim()
                val quality = item.optString("q").trim()
                if (id.isNotBlank() && mirror.isNotBlank()) add(MirrorEntry(id, mirror, quality))
            }
        }
    }

    private fun decodeBase64(value: String): String? {
        val clean = value.trim()
            .replace("-", "+")
            .replace("_", "/")
        return runCatching { String(Base64.decode(clean, Base64.DEFAULT), Charsets.UTF_8) }.getOrNull()
    }

    private fun resolveUrl(base: String, candidate: String): String? =
        runCatching { URI(base).resolve(candidate).toString() }.getOrNull()

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
            else -> StreamType.MP4
        }
    }

    private data class MirrorEntry(val id: String, val i: String, val q: String)
    private data class DownloadCandidate(val url: String, val quality: String?)

    private companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
