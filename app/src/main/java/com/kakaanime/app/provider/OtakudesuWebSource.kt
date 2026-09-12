package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * HTML-first Otakudesu source adapter.
 *
 * The old API endpoints are treated as optional accelerators. The web source
 * is the source of truth for detail/episode discovery because mirrors and API
 * deployments change independently.
 */
internal class OtakudesuWebSource {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val sources = listOf(
        WebSource("fit", "https://otakudesu.fit", true),
        WebSource("blog", "https://otakudesu.blog", false),
        WebSource("ro", "https://otakudesu.ro", false),
        WebSource("cloud", "https://otakudesu.cloud", false)
    )

    suspend fun getAnime(slug: String): WebAnime? {
        for (source in sources) {
            for (url in source.detailUrls(slug)) {
                val html = get(url) ?: continue
                if (!html.contains("One Piece", ignoreCase = true) &&
                    !html.contains("anime", ignoreCase = true)) continue
                val title = html.firstMatch(
                    "<h1[^>]*>(.*?)</h1>",
                    "<title[^>]*>(.*?)</title>"
                )?.stripHtml()?.removeSuffix(" - Otaku desu")?.trim()
                if (title.isNullOrBlank()) continue

                return WebAnime(
                    url = url,
                    title = title,
                    html = html,
                    sourceId = source.id
                )
            }
        }
        return null
    }

    suspend fun getEpisodes(anime: WebAnime): List<WebEpisode> =
        parseEpisodes(anime.url, anime.html)

    suspend fun getEpisodePage(episode: WebEpisode): String? = get(episode.url)

    fun discoverPlaybackUrls(html: String, pageUrl: String): List<String> {
        val urls = linkedSetOf<String>()
        val patterns = listOf(
            Regex("<iframe[^>]+(?:src|data-src)=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("(?:data-video|data-src|data-url)=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("<source[^>]+src=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE),
            Regex("<video[^>]+src=[\\\"']([^\\\"']+)[\\\"']", RegexOption.IGNORE_CASE)
        )

        patterns.forEach { pattern ->
            pattern.findAll(html).forEach { match ->
                val raw = decodeHtml(match.groupValues[1].trim())
                resolve(pageUrl, raw)?.let { urls += it }
            }
        }
        return urls.toList()
    }

    private fun parseEpisodes(pageUrl: String, html: String): List<WebEpisode> {
        val result = mutableListOf<WebEpisode>()
        val pattern = Regex(
            "<a[^>]+href=[\\\"']([^\\\"']+)[\\\"'][^>]*>(.*?)</a>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        for (match in pattern.findAll(html)) {
            val href = match.groupValues[1].trim()
            val text = match.groupValues[2].stripHtml().trim()
            val number = extractEpisodeNumber(text, href) ?: continue
            val url = resolve(pageUrl, decodeHtml(href)) ?: continue
            if (!url.contains("episode", ignoreCase = true)) continue
            result += WebEpisode(
                url = url,
                number = number,
                title = text.ifBlank { "Episode $number" }
            )
        }

        return result.distinctBy { it.number }.sortedBy { it.number }
    }

    private suspend fun get(url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()
            }
        }.getOrNull()
    }

    private fun WebSource.detailUrls(slug: String): List<String> {
        val clean = slug.removePrefix("otakudesu:").trim('/')
        val fitSlug = clean
            .replace(Regex("-(?:subtitle-)?indonesia$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("-sub-indo$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^1piece$", RegexOption.IGNORE_CASE), "one-piece")
            .replace(Regex("^onepiece$", RegexOption.IGNORE_CASE), "one-piece")
        return if (seriesStyle) {
            listOf("$baseUrl/series/${fitSlug.trim('/')}/")
        } else {
            listOf(
                "$baseUrl/anime/${clean.trim('/')}/",
                "$baseUrl/anime/${clean.trim('/').removeSuffix("-sub-indo")}/"
            ).distinct()
        }
    }

    private fun String.firstMatch(vararg patterns: String): String? {
        for (pattern in patterns) {
            val match = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .find(this)?.groupValues?.getOrNull(1)
            if (!match.isNullOrBlank()) return match
        }
        return null
    }

    private fun String.stripHtml(): String =
        replace(Regex("<[^>]+>"), " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun decodeHtml(value: String): String =
        value.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

    private fun resolve(base: String, candidate: String): String? =
        runCatching { URI(base).resolve(candidate).toString() }.getOrNull()

    private fun extractEpisodeNumber(title: String, href: String): Int? {
        val value = "$title $href"
        return Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE)
            .find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("(?:-|/)(\\d+)(?:-|/|$)")
                .find(href)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private data class WebSource(
        val id: String,
        val baseUrl: String,
        val seriesStyle: Boolean
    )

    internal data class WebAnime(
        val url: String,
        val title: String,
        val html: String,
        val sourceId: String
    )

    internal data class WebEpisode(
        val url: String,
        val number: Int,
        val title: String
    )
}
