package com.kakaanime.app.provider

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Diagnostic-only test for the Samehadaku stream boundary.
 * It intentionally does not resolve or play media; it records the actual
 * episode server/embed data so the next resolver fix is based on evidence.
 */
@RunWith(AndroidJUnit4::class)
class SamehadakuEpisodeDiagnosticsTest {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    @Test
    fun onePieceEpisodeSevenReportsServerHosts() = runBlocking {
        val provider = SamehadakuProvider()
        val results = provider.search("One Piece")
        val anime = results.firstOrNull {
            it.title.trim().equals("One Piece", ignoreCase = true) ||
                it.id.trimEnd('/').endsWith("/anime/one-piece", ignoreCase = true)
        }
        assertNotNull("Canonical One Piece was not found", anime)

        val episodes = provider.getEpisodes(anime!!.id)
        val episode = episodes.firstOrNull { it.number == 7 }
        assertNotNull("Episode 7 was not found", episode)

        val episodeUrl = episode!!.id.removePrefix("samehadaku:")
        assertTrue("Episode 7 URL is invalid: $episodeUrl", episodeUrl.startsWith("http", true))
        Log.i(TAG, "DIAG_EPISODE_URL=$episodeUrl")

        val response = get(episodeUrl, "$MAIN_URL/")
        assertNotNull("Episode page request failed", response)
        val finalUrl = response!!.request.url.toString()
        val html = response.body?.string().orEmpty()
        assertTrue("Episode page returned empty HTML", html.isNotBlank())
        Log.i(TAG, "DIAG_EPISODE_HTTP code=${response.code} finalUrl=$finalUrl htmlLength=${html.length}")

        val document = Jsoup.parse(html, finalUrl)
        val downloadLinks = document.select("div#downloadb li a[href]")
            .mapNotNull { it.absUrl("href").ifBlank { it.attr("href") }.trim().takeIf { it.startsWith("http", true) } }
            .distinct()
        Log.i(TAG, "DIAG_DOWNLOAD_LINK_COUNT=${downloadLinks.size}")
        downloadLinks.forEachIndexed { index, url ->
            Log.i(TAG, "DIAG_DOWNLOAD[$index] host=${host(url)} url=${url.take(300)}")
        }

        val servers = document.select("#server > ul > li > div")
        Log.i(TAG, "DIAG_SERVER_COUNT=${servers.size}")
        servers.forEachIndexed { index, server ->
            val post = server.attr("data-post").trim()
            val nume = server.attr("data-nume").trim()
            val type = server.attr("data-type").trim()
            val label = server.selectFirst("span")?.text()?.trim().orEmpty()
            Log.i(TAG, "DIAG_SERVER[$index] label=$label post=$post nume=$nume type=$type")

            if (post.isBlank() || nume.isBlank() || type.isBlank()) return@forEachIndexed
            val ajax = playerAjax(finalUrl, post, nume, type)
            if (ajax == null) {
                Log.i(TAG, "DIAG_AJAX[$index] request_failed")
                return@forEachIndexed
            }
            val ajaxHtml = ajax.body?.string().orEmpty()
            Log.i(TAG, "DIAG_AJAX[$index] code=${ajax.code} length=${ajaxHtml.length}")
            val ajaxDoc = Jsoup.parse(ajaxHtml, finalUrl)
            val iframe = ajaxDoc.selectFirst("iframe[src], iframe[data-src]")?.let { frame ->
                frame.absUrl("src").ifBlank { frame.attr("src") }
                    .ifBlank { frame.absUrl("data-src") }
                    .ifBlank { frame.attr("data-src") }
                    .trim()
            }
            Log.i(TAG, "DIAG_AJAX[$index] iframeHost=${iframe?.let(::host) ?: "none"} iframe=${iframe?.take(300) ?: "none"}")
            logCandidateUrls(index, ajaxHtml, finalUrl)
        }

        val pageIframes = document.select("iframe[src], iframe[data-src]")
            .mapNotNull { frame ->
                frame.absUrl("src").ifBlank { frame.attr("src") }
                    .ifBlank { frame.absUrl("data-src") }
                    .ifBlank { frame.attr("data-src") }
                    .trim()
                    .takeIf { it.startsWith("http", true) }
            }
            .distinct()
        Log.i(TAG, "DIAG_PAGE_IFRAME_COUNT=${pageIframes.size}")
        pageIframes.forEachIndexed { index, url ->
            Log.i(TAG, "DIAG_PAGE_IFRAME[$index] host=${host(url)} url=${url.take(300)}")
        }
    }

    private fun get(url: String, referer: String): okhttp3.Response? = runCatching {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Referer", referer)
            .build()
        client.newCall(request).execute()
    }.getOrNull()

    private fun playerAjax(episodeUrl: String, post: String, nume: String, type: String): okhttp3.Response? = runCatching {
        val body = FormBody.Builder()
            .add("action", "player_ajax")
            .add("post", post)
            .add("nume", nume)
            .add("type", type)
            .build()
        val request = Request.Builder()
            .url("$MAIN_URL/wp-admin/admin-ajax.php")
            .post(body)
            .header("User-Agent", USER_AGENT)
            .header("Referer", episodeUrl)
            .header("Origin", MAIN_URL)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Accept", "*/*")
            .build()
        client.newCall(request).execute()
    }.getOrNull()

    private fun logCandidateUrls(index: Int, html: String, baseUrl: String) {
        val document = Jsoup.parse(html, baseUrl)
        val urls = linkedSetOf<String>()
        document.select("a[href], iframe[src], iframe[data-src], source[src], source[data-src], video[src], video[data-src]")
            .forEach { element ->
                listOf("href", "src", "data-src").forEach { attribute ->
                    val value = element.absUrl(attribute).ifBlank { element.attr(attribute) }.trim()
                    if (value.startsWith("http", true)) urls += value
                }
            }
        Regex("https?://[^\\s\"'<>]+")
            .findAll(html)
            .map { it.value.trimEnd(')', ',', ';') }
            .forEach { urls += it }
        urls.take(20).forEachIndexed { candidateIndex, url ->
            Log.i(TAG, "DIAG_AJAX[$index]_URL[$candidateIndex] host=${host(url)} url=${url.take(300)}")
        }
    }

    private fun host(url: String): String =
        runCatching { URI(url).host.orEmpty().lowercase() }.getOrDefault("invalid")

    private companion object {
        const val TAG = "AniLab-Samehadaku-DIAG"
        const val MAIN_URL = "https://v2.samehadaku.how"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    }
}
