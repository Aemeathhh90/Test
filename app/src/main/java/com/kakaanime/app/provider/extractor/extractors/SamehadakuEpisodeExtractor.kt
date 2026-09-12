package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.ExtractorRegistry
import com.kakaanime.app.provider.extractor.StreamExtractor
import com.kakaanime.app.provider.extractor.StreamResolver
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Samehadaku episode-page resolver.
 *
 * The site does not reliably expose a playable URL directly on the episode
 * page. Current CloudStream implementations first discover download/server
 * links, then hand each host URL to a host extractor. This extractor mirrors
 * that boundary inside AniLab without depending on CloudStream.
 */
class SamehadakuEpisodeExtractor : StreamExtractor {
    override val id = "samehadaku-episode"
    override val priority = 120

    private val mainHost = "v2.samehadaku.how"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val hostResolver = StreamResolver(
        ExtractorRegistry(includeSamehadakuEpisodeExtractor = false)
    )

    override fun canHandle(url: String): Boolean {
        val host = runCatching { URI(url).host.orEmpty().lowercase() }.getOrDefault("")
        if (!host.contains(mainHost)) return false
        return url.contains("episode", ignoreCase = true) ||
            url.contains("/eps-", ignoreCase = true) ||
            url.contains("/one-piece-", ignoreCase = true)
    }

    override suspend fun extract(
        url: String,
        referer: String?
    ): List<ProviderStream> {
        val page = getPage(url) ?: return emptyList()
        val discovered = linkedMapOf<String, DiscoveredLink>()

        // Reference implementations use the download list first.
        page.select("div#downloadb li a[href]").forEach { anchor ->
            val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }.trim()
            if (href.startsWith("http", true)) {
                val quality = anchor.parent()?.selectFirst("strong")?.text()?.trim()
                    ?: anchor.closest("li")?.selectFirst("strong")?.text()?.trim()
                discovered.putIfAbsent(href, DiscoveredLink(href, quality))
            }
        }

        // Newer/current pages can expose servers that require player_ajax.
        page.select("#server > ul > li > div").forEach { server ->
            val post = server.attr("data-post").trim()
            val nume = server.attr("data-nume").trim()
            val type = server.attr("data-type").trim()
            if (post.isBlank() || nume.isBlank() || type.isBlank()) return@forEach

            val embed = requestPlayerAjax(url, post, nume, type)
                ?: return@forEach
            discovered.putIfAbsent(
                embed,
                DiscoveredLink(
                    embed,
                    server.selectFirst("span")?.text()?.trim()
                )
            )
        }

        // Last-resort iframe fallback for older episode layouts.
        if (discovered.isEmpty()) {
            page.selectFirst("iframe[src], iframe[data-src]")?.let { iframe ->
                val href = iframe.absUrl("src").ifBlank { iframe.attr("src") }
                    .ifBlank { iframe.attr("data-src") }
                    .trim()
                if (href.startsWith("http", true)) {
                    discovered[href] = DiscoveredLink(href, null)
                }
            }
        }

        if (discovered.isEmpty()) return emptyList()

        val streams = mutableListOf<ProviderStream>()
        for ((_, link) in discovered) {
            val resolved = when {
                isDirectMedia(link.url) -> listOf(directStream(link.url, url, link.quality))
                else -> hostResolver.resolve(
                    urls = listOf(link.url),
                    referer = url
                )
            }
            streams += resolved.map { stream ->
                if (link.quality.isNullOrBlank()) stream
                else stream.copy(quality = link.quality)
            }
        }

        return streams.distinctBy { it.url }
    }

    private fun getPage(url: String): org.jsoup.nodes.Document? = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "id-ID,id;q=0.9,en;q=0.8")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            response.body?.string()?.takeIf { it.isNotBlank() }?.let { Jsoup.parse(it, url) }
        }
    }.getOrNull()

    private fun requestPlayerAjax(
        episodeUrl: String,
        post: String,
        nume: String,
        type: String
    ): String? = runCatching {
        val body = FormBody.Builder()
            .add("action", "player_ajax")
            .add("post", post)
            .add("nume", nume)
            .add("type", type)
            .build()

        val request = Request.Builder()
            .url("https://$mainHost/wp-admin/admin-ajax.php")
            .post(body)
            .header("User-Agent", USER_AGENT)
            .header("Referer", episodeUrl)
            .header("Origin", "https://$mainHost")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Accept", "*/*")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@runCatching null
            val html = response.body?.string().orEmpty()
            extractUrl(html, episodeUrl)
        }
    }.getOrNull()

    private fun extractUrl(value: String, baseUrl: String): String? {
        val document = Jsoup.parse(value, baseUrl)
        val fromIframe = document.selectFirst("iframe[src], iframe[data-src]")?.let { iframe ->
            iframe.absUrl("src").ifBlank { iframe.attr("src") }
                .ifBlank { iframe.attr("data-src") }
        }
        if (!fromIframe.isNullOrBlank()) return fromIframe.trim()

        val regex = Regex("(?:src|file|source|url)\\s*[:=]\\s*[\\\"']([^\\\"']+)")
        return regex.find(value)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun isDirectMedia(url: String): Boolean =
        url.contains(".m3u8", true) ||
            url.contains(".mpd", true) ||
            url.contains(".mp4", true) ||
            url.contains(".webm", true)

    private fun directStream(url: String, referer: String, quality: String?): ProviderStream =
        ProviderStream(
            providerId = "samehadaku",
            url = url,
            quality = quality,
            language = "Japanese",
            subtitleLanguage = "Indonesian",
            type = when {
                url.contains(".m3u8", true) -> StreamType.HLS
                url.contains(".mpd", true) -> StreamType.DASH
                url.contains(".mp4", true) || url.contains(".webm", true) -> StreamType.MP4
                else -> StreamType.UNKNOWN
            },
            headers = mapOf(
                "User-Agent" to USER_AGENT,
                "Referer" to referer
            )
        )

    private data class DiscoveredLink(
        val url: String,
        val quality: String?
    )

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    }
}
