package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.extractor.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * API fallback for Otakudesu episode pages.
 *
 * Some Otakudesu deployments expose episode URLs whose slug differs from the
 * canonical slug used by qrtzanim. This extractor uses the episode page's
 * canonical URL (or title as a fallback) to query qrtzanim, then sends the
 * returned embed URLs through the normal generic host resolver.
 */
class OtakudesuQrtzExtractor : StreamExtractor {
    override val id = "otakudesu-qrtz"
    override val priority = 90

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    private val genericEmbed = GenericEmbedExtractor()

    override fun canHandle(url: String): Boolean {
        val value = url.trim().lowercase()
        return value.startsWith("http") &&
            value.contains("otakudesu.") &&
            (value.contains("/episode/") || value.contains("/episodes/") || value.contains("-episode-"))
    }

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> =
        withContext(Dispatchers.IO) {
            val page = get(url) ?: return@withContext emptyList()
            val slug = canonicalEpisodeSlug(url, page) ?: return@withContext emptyList()
            val json = getJson("https://qrtzanim.vercel.app/api/episode/${encodePath(slug)}")
                ?: return@withContext emptyList()
            val data = json.optJSONObject("data") ?: json.optJSONObject("episode_detail") ?: return@withContext emptyList()

            val candidates = linkedMapOf<String, String?>()
            data.optString("defaultStreamUrl").trim().takeIf { it.isNotBlank() }?.let { candidates[it] = null }

            val streams = data.optJSONArray("streams") ?: JSONArray()
            for (i in 0 until streams.length()) {
                val item = streams.optJSONObject(i) ?: continue
                val candidate = listOf("embedUrl", "streamUrl", "url", "link")
                    .asSequence()
                    .map { item.optString(it).trim() }
                    .firstOrNull { it.isNotBlank() }
                    ?: continue
                val quality = listOf("resolution", "quality")
                    .asSequence()
                    .map { item.optString(it).trim() }
                    .firstOrNull { it.isNotBlank() }
                candidates.putIfAbsent(candidate, quality)
            }

            val results = mutableListOf<ProviderStream>()
            for ((candidate, quality) in candidates) {
                if (candidate.isDirectMediaUrl()) {
                    results += ProviderStream(
                        providerId = "otakudesu",
                        url = candidate,
                        quality = quality,
                        type = candidate.toStreamType(),
                        headers = mapOf("Referer" to url)
                    )
                    continue
                }
                val resolved = runCatching { genericEmbed.extract(candidate, url) }
                    .getOrDefault(emptyList())
                results += resolved.map { stream ->
                    stream.copy(
                        providerId = "otakudesu",
                        quality = stream.quality ?: quality
                    )
                }
            }
            results.distinctBy { it.url }
        }

    private fun get(url: String): String? =
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", UA)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()
            }
        }.getOrNull()

    private fun getJson(url: String): JSONObject? =
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KakaAnime/0.1")
                .header("Accept", "application/json,text/plain,*/*")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string().orEmpty().trim()
                if (body.isBlank()) null else JSONObject(body)
            }
        }.getOrNull()

    private fun canonicalEpisodeSlug(pageUrl: String, html: String): String? {
        val canonical = Regex(
            "<link[^>]+rel=[\\\"']canonical[\\\"'][^>]+href=[\\\"']([^\\\"']+)[\\\"']",
            RegexOption.IGNORE_CASE
        ).find(html)?.groupValues?.getOrNull(1)
            ?: Regex(
                "<meta[^>]+property=[\\\"']og:url[\\\"'][^>]+content=[\\\"']([^\\\"']+)[\\\"']",
                RegexOption.IGNORE_CASE
            ).find(html)?.groupValues?.getOrNull(1)

        val canonicalPath = canonical?.let { runCatching { URI(pageUrl).resolve(it).path }.getOrNull() }
        val canonicalSlug = canonicalPath
            ?.substringAfterLast("/episode/", "")
            ?.trim('/')
            ?.takeIf { it.isNotBlank() }
        if (canonicalSlug != null) return canonicalSlug

        val title = Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(html)?.groupValues?.getOrNull(1)
            ?.replace(Regex("<[^>]+>"), " ")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?.replace(Regex("\\s*-\\s*Otaku.*$", RegexOption.IGNORE_CASE), "")
            ?.trim()

        val episode = Regex("(?:episode|eps|ep)\\s*([0-9]+(?:\\.[0-9]+)?)", RegexOption.IGNORE_CASE)
            .find("$title $pageUrl")?.groupValues?.getOrNull(1)
            ?: return null
        val animeTitle = title
            ?.replace(Regex("\\b(?:episode|eps|ep)\\s*$episode.*$", RegexOption.IGNORE_CASE), "")
            ?.replace(Regex("\\s+(?:subtitle|sub)\\s+indonesia.*$", RegexOption.IGNORE_CASE), "")
            ?.trim()
            ?: return null
        val animeSlug = animeTitle
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return "$animeSlug-episode-${episode.removeSuffix(".0")}-sub-indo"
    }

    private fun encodePath(value: String): String =
        value.trim('/').split('/').joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8") }

    private fun String.isDirectMediaUrl(): Boolean {
        val clean = substringBefore('?').substringBefore('#').lowercase()
        return clean.endsWith(".m3u8") || clean.endsWith(".mpd") ||
            clean.endsWith(".mp4") || clean.endsWith(".mkv") || clean.endsWith(".webm")
    }

    private fun String.toStreamType() = when {
        substringBefore('?').lowercase().endsWith(".m3u8") -> com.kakaanime.app.provider.StreamType.HLS
        substringBefore('?').lowercase().endsWith(".mpd") -> com.kakaanime.app.provider.StreamType.DASH
        else -> com.kakaanime.app.provider.StreamType.MP4
    }

    private companion object {
        const val UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
