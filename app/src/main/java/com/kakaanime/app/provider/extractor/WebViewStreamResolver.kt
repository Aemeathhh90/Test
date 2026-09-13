package com.kakaanime.app.provider.extractor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Resolves JavaScript-driven player pages by observing media requests made by
 * Android WebView. It captures the first m3u8, mpd, mp4 or webm request without
 * intercepting or modifying the response itself.
 */
class WebViewStreamResolver(
    private val context: Context,
    private val timeoutMs: Long = 20_000L
) {
    suspend fun resolve(
        url: String,
        referer: String? = null,
        quality: String? = null
    ): List<ProviderStream> = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean(false)
            val mainHandler = Handler(Looper.getMainLooper())
            var webView: WebView? = null

            fun finish(result: List<ProviderStream>) {
                if (!finished.compareAndSet(false, true)) return
                mainHandler.post {
                    mainHandler.removeCallbacksAndMessages(null)
                    webView?.stopLoading()
                    webView?.destroy()
                    webView = null
                    if (continuation.isActive) continuation.resume(result)
                }
            }

            fun capture(candidate: String, requestHeaders: Map<String, String> = emptyMap()) {
                val type = candidate.toStreamType() ?: return
                val cookie = runCatching {
                    CookieManager.getInstance().getCookie(candidate)
                }.getOrNull()
                val headers = buildMap {
                    put("User-Agent", USER_AGENT)
                    referer?.takeIf { it.isNotBlank() }?.let { put("Referer", it) }
                    requestHeaders["Referer"]?.takeIf { it.isNotBlank() }?.let { put("Referer", it) }
                    cookie?.takeIf { it.isNotBlank() }?.let { put("Cookie", it) }
                }
                finish(listOf(ProviderStream(
                    providerId = "webview-resolver",
                    url = candidate,
                    quality = quality,
                    type = type,
                    headers = headers
                )))
            }

            mainHandler.postDelayed({ finish(emptyList()) }, timeoutMs)

            webView = WebView(context.applicationContext).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.userAgentString = USER_AGENT
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        if (!request.isForMainFrame) {
                            capture(request.url.toString(), request.requestHeaders)
                        }
                        return null
                    }

                    @Suppress("DEPRECATION")
                    override fun shouldInterceptRequest(
                        view: WebView,
                        url: String
                    ): WebResourceResponse? {
                        capture(url)
                        return null
                    }
                }
            }

            continuation.invokeOnCancellation {
                mainHandler.post {
                    if (finished.compareAndSet(false, true)) {
                        mainHandler.removeCallbacksAndMessages(null)
                        webView?.stopLoading()
                        webView?.destroy()
                        webView = null
                    }
                }
            }

            runCatching {
                val headers = buildMap {
                    put("Accept-Language", "id-ID,id;q=0.9,en;q=0.8")
                    referer?.takeIf { it.isNotBlank() }?.let { put("Referer", it) }
                }
                webView?.loadUrl(url, headers)
            }.onFailure {
                finish(emptyList())
            }
        }
    }

    private fun String.toStreamType(): StreamType? {
        val value = lowercase()
        return when {
            value.contains(".m3u8") -> StreamType.HLS
            value.contains(".mpd") -> StreamType.DASH
            value.contains(".mp4") || value.contains(".webm") -> StreamType.MP4
            else -> null
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    }
}
