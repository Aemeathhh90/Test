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
 * CloudStream-style flow:
 * episode page -> mirror/server discovery -> host extractor -> final media.
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
        return value.startsWith("http") && value.contains("otakudesu.") &&
            (value.contains("/episode/") || value.contains("/episodes/") || value.contains("-episode-"))
    }

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> = withContext(Dispatchers.IO) {
        val html = get(url, referer) ?: return@withContext emptyList()
        val results = linkedMapOf<String, ProviderStream>()

        resolveMirrorStream(url, html).forEach { candidate ->
            resolveExternal(candidate, url).forEach { stream -> results.putIfAbsent(stream.url, stream) }
        }
        discoverDownloadLinks(url, html).forEach { candidate ->
            resolveExternal(candidate.url, url, candidate.quality).forEach { stream -> results.putIfAbsent(stream.url, stream) }
        }
        if (results.isEmpty()) {
            extractInlineMedia(html, url).forEach { media ->
                results.putIfAbsent(media, ProviderStream("otakudesu", media, type = media.toStreamType(), headers = mapOf("Referer" to url)))
            }
        }
        results.values.toList()
    }

    private suspend fun resolveMirrorStream(pageUrl: String, html: String): List<String> {
        val host = runCatching { URI(pageUrl).let { "${it.scheme}://${it.authority}" } }.getOrNull() ?: return emptyList()
        val scripts = Regex("<script[^>]*>(.*?)</script>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .findAll(html).map { it.groupValues[1] }.filter { it.contains("action:", true) }.toList()
        if (scripts.isEmpty()) return emptyList()

        val actions = scripts.asSequence().flatMap { script ->
            Regex("(?:^|[,\\{\\s])action\\s*:\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
                .findAll(script).map { it.groupValues[1] }.asSequence()
        }.distinct().toList()
        if (actions.size < 2) return emptyList()

        val nonce = postAjax(host, mapOf("action" to actions.first())) ?: return emptyList()
        val action = actions[1]
        val mirrorEntries = Regex("(?:data-content|data-video|data-src)\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
            .findAll(html).flatMap { match ->
                val raw = decodeHtml(match.groupValues[1])
                (decodeBase64(raw)?.let { parseMirrorEntries(it) } ?: parseMirrorEntries(raw)).asSequence()
            }.toList()

        val results = linkedSetOf<String>()
        for (entry in mirrorEntries.distinctBy { "${it.id}|${it.i}|${it.q}" }) {
            val response = postAjax("$host/wp-admin/admin-ajax.php", mapOf("id" to entry.id, "i" to entry.i, "q" to entry.q, "nonce" to nonce, "action" to action)) ?: continue
            val decoded = decodeBase64(response) ?: response
            extractIframeUrls(decoded, pageUrl).forEach(results::add)
            extractInlineMedia(decoded, pageUrl).forEach(results::add)
        }
        return results.toList()
    }

    private suspend fun discoverDownloadLinks(pageUrl: String, html: String): List<DownloadCandidate> {
        val result = mutableListOf<DownloadCandidate>()
        val liPattern = Regex("<li[^>]*>(.*?)</li>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        for (li in liPattern.findAll(html)) {
            val block = li.groupValues[1]
            if (!block.contains("href=", true)) continue
            val quality = Regex("(\\d{3,4})\\s*[pP]").find(block)?.groupValues?.getOrNull(1)
            Regex("href\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE).findAll(block).forEach { match ->
                resolveUrl(pageUrl, decodeHtml(match.groupValues[1]))?.let { result += DownloadCandidate(it, quality) }
            }
        }
        return result.distinctBy { it.url }
    }

    private suspend fun resolveExternal(url: String, referer: String, quality: String? = null): List<ProviderStream> {
        val clean = resolveRedirect(url, referer) ?: url
        val normalizedQuality = quality ?: Regex("(\\d{3,4})[pP]").find(clean)?.groupValues?.getOrNull(1)
        return when {
            clean.isDirectMediaUrl() -> listOf(ProviderStream("otakudesu", clean, quality = normalizedQuality, type = clean.toStreamType(), headers = mapOf("Referer" to referer)))
            clean.contains("pixeldrain.com", true) -> pixeldrain.extract(clean, referer).map { it.copy(quality = it.quality ?: normalizedQuality) }
            clean.contains("krakenfiles.com", true) -> kraken.extract(clean, referer).map { it.copy(quality = it.quality ?: normalizedQuality) }
            else -> genericEmbed.extract(clean, referer).map { it.copy(providerId = "otakudesu", quality = it.quality ?: normalizedQuality) }
        }
    }

    private fun extractIframeUrls(html: String, baseUrl: String): List<String> = Regex("<iframe[^>]+(?:src|data-src)\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
        .findAll(html).mapNotNull { resolveUrl(baseUrl, decodeHtml(it.groupValues[1])) }.distinct().toList()

    private fun extractInlineMedia(html: String, baseUrl: String): List<String> {
        val urls = linkedSetOf<String>()
        listOf(
            Regex("(?:file|src|source|hls|m3u8|videoUrl|video_url|playlist)\\s*[=:]\\s*[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("https?://[^\\s\\\"'<>]+\\.(?:m3u8|mpd|mp4|mkv|webm)(?:\\?[^\\s\\\"'<>]*)?", RegexOption.IGNORE_CASE)
        ).forEach { pattern ->
            pattern.findAll(html).forEach { match ->
                val raw = match.groupValues.getOrNull(1)?.ifBlank { match.value } ?: return@forEach
                resolveUrl(baseUrl, decodeHtml(raw))?.takeIf { it.isDirectMediaUrl() }?.let(urls::add)
            }
        }
        return urls.toList()
    }

    private suspend fun resolveRedirect(url: String, referer: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).header("User-Agent", UA).header("Referer", referer).build()
            client.newCall(request).execute().use { response -> response.request.url.toString() }
        }.getOrNull()
    }

    private suspend fun get(url: String, referer: String?): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).header("User-Agent", UA).header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8").apply { if (!referer.isNullOrBlank()) header("Referer", referer) }.build()
            client.newCall(request).execute().use { response -> if (response.isSuccessful) response.body?.string() else null }
        }.getOrNull()
    }

    private suspend fun postAjax(endpoint: String, fields: Map<String, String>): String? = withContext(Dispatchers.IO) {
        runCatching {
            val body = FormBody.Builder().apply { fields.forEach { (k, v) -> add(k, v) } }.build()
            val request = Request.Builder().url(if (endpoint.endsWith("admin-ajax.php")) endpoint else "$endpoint/wp-admin/admin-ajax.php").post(body).header("User-Agent", UA).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()?.trim()?.takeIf { it.isNotBlank() }?.let { raw -> runCatching { JSONObject(raw).optString("data").ifBlank { raw } }.getOrDefault(raw) }
            }
        }.getOrNull()
    }

    private fun parseMirrorEntries(value: String): List<MirrorEntry> {
        val array = runCatching { JSONArray(value.trim()) }.getOrNull() ?: return emptyList()
        return buildList { for (i in 0 until array.length()) { val item = array.optJSONObject(i) ?: continue; val id = item.optString("id").trim(); val mirror = item.optString("i").trim(); val q = item.optString("q").trim(); if (id.isNotBlank() && mirror.isNotBlank()) add(MirrorEntry(id, mirror, q)) } }
    }

    private fun decodeBase64(value: String?): String? = runCatching { String(Base64.decode(value?.trim()?.replace("-", "+")?.replace("_", "/"), Base64.DEFAULT), Charsets.UTF_8) }.getOrNull()
    private fun resolveUrl(base: String, candidate: String): String? = runCatching { URI(base).resolve(candidate).toString() }.getOrNull()
    private fun decodeHtml(value: String): String = value.replace("&amp;", "&").replace("&quot;", "\"").replace("&#039;", "'").replace("&lt;", "<").replace("&gt;", ">")
    private fun String.isDirectMediaUrl(): Boolean { val clean = substringBefore('?').substringBefore('#').lowercase(); return clean.endsWith(".m3u8") || clean.endsWith(".mpd") || clean.endsWith(".mp4") || clean.endsWith(".mkv") || clean.endsWith(".webm") }
    private fun String.toStreamType(): StreamType { val clean = substringBefore('?').substringBefore('#').lowercase(); return when { clean.endsWith(".m3u8") -> StreamType.HLS; clean.endsWith(".mpd") -> StreamType.DASH; else -> StreamType.MP4 } }

    private data class MirrorEntry(val id: String, val i: String, val q: String)
    private data class DownloadCandidate(val url: String, val quality: String?)
    private companion object { const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36" }
}
