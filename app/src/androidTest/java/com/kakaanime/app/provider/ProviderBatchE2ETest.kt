package com.kakaanime.app.provider

import android.view.SurfaceView
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kakaanime.app.MainActivity
import com.kakaanime.provider.AnimeProvider
import com.kakaanime.provider.ProviderStream
import com.kakaanime.provider.SamehadakuProvider
import com.kakaanime.provider.StreamType
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ProviderBatchE2ETest {
    @Test
    fun samehadakuFirstFrameDiagnostic() = runBlocking {
        val provider = SamehadakuProvider(null)
        val label = "Samehadaku-native"
        diagnoseSearchNetwork()
        val result = try {
            runProvider(label, provider)
        } catch (t: Throwable) {
            "ERROR ${t.javaClass.simpleName}: ${t.message}"
        }
        println("========== KAKAANIME SAMEHADAKU FIRST FRAME ==========")
        println("$label = $result")
        println("=======================================================")
        assertTrue("Diagnostic completed; inspect result above", result.isNotBlank())
    }

    private fun diagnoseSearchNetwork() {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
        val q = URLEncoder.encode("One Piece", "UTF-8")
        val urls = listOf(
            "https://v2.samehadaku.how/?s=$q",
            "https://v2.samehadaku.how/search/one-piece/",
            "https://v2.samehadaku.how/search/?q=$q",
            "https://v2.samehadaku.how/anime/one-piece/"
        )
        for (url in urls) {
            runCatching {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Referer", "https://v2.samehadaku.how/")
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    val marker = listOf("one piece", "animepost", "animpost", "listupd", "entry-title")
                        .firstOrNull { body.contains(it, ignoreCase = true) } ?: "none"
                    println("[KAKA-CCTV][Samehadaku-search] code=${response.code} final=${response.request.url} bytes=${body.length} marker=$marker")
                }
            }.onFailure { t ->
                println("[KAKA-CCTV][Samehadaku-search] ERROR url=$url type=${t.javaClass.simpleName} msg=${t.message}")
            }
        }
    }

    private suspend fun runProvider(label: String, provider: AnimeProvider): String {
        val results = provider.search("One Piece")
        if (results.isEmpty()) return "SEARCH_FAILED"
        val anime = results.firstOrNull { it.title.equals("One Piece", true) || it.id.contains("one-piece", true) } ?: results.first()
        if (provider.getAnime(anime.id) == null) return "DETAIL_FAILED id=${anime.id}"
        val episodes = provider.getEpisodes(anime.id)
        val episode = episodes.firstOrNull { it.number == 7 } ?: return "EPISODE_FAILED count=${episodes.size}"
        val streams = provider.getStreams(anime.id, episode.number)
        if (streams.isEmpty()) return "STREAM_FAILED"
        val selected = streams.filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
            .sortedByDescending { score(it.type) }
            .firstOrNull() ?: return "HTTP_STREAM_FAILED"
        println("[KAKA-CCTV][$label] STREAM type=${selected.type} quality=${selected.quality ?: "unknown"} url=${selected.url.take(180)} headers=${selected.headers}")
        if (selected.type == StreamType.UNKNOWN) return "UNKNOWN_STREAM_TYPE"
        return renderFirstFrame(label, selected)
    }

    private fun score(type: StreamType) = when (type) {
        StreamType.HLS, StreamType.DASH -> 3
        StreamType.MP4 -> 2
        StreamType.UNKNOWN -> 0
    }

    private fun renderFirstFrame(label: String, stream: ProviderStream): String {
        val rendered = CountDownLatch(1)
        var error: String? = null
        var player: ExoPlayer? = null
        var surfaceView: SurfaceView? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        return try {
            scenario.onActivity { activity ->
                val content = activity.findViewById<ViewGroup>(android.R.id.content)
                val surface = SurfaceView(activity).apply {
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                    setZOrderOnTop(false)
                }
                surfaceView = surface
                content.addView(surface)

                val http = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36")
                    .setAllowCrossProtocolRedirects(true)
                    .setDefaultRequestProperties(
                        LinkedHashMap(stream.headers).apply {
                            putIfAbsent("Accept", "*/*")
                            putIfAbsent("Referer", "https://v2.samehadaku.how/")
                        }
                    )

                player = ExoPlayer.Builder(activity)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(activity, http)))
                    .build()
                    .also { exo ->
                        exo.addAnalyticsListener(object : AnalyticsListener {
                            override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
                                println("[KAKA-CCTV][$label] FIRST_FRAME_RENDERED")
                                rendered.countDown()
                            }

                            override fun onPlayerError(eventTime: AnalyticsListener.EventTime, playbackException: androidx.media3.common.PlaybackException) {
                                error = playbackException.errorCodeName + " " + (playbackException.message ?: "")
                                println("[KAKA-CCTV][$label] PLAYER_ERROR $error")
                                rendered.countDown()
                            }

                            override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
                                println("[KAKA-CCTV][$label] STATE=$state")
                            }
                        })
                        exo.setVideoSurfaceView(surface)
                        val item = MediaItem.Builder().setUri(stream.url).apply {
                            when (stream.type) {
                                StreamType.HLS -> setMimeType(MimeTypes.APPLICATION_M3U8)
                                StreamType.DASH -> setMimeType(MimeTypes.APPLICATION_MPD)
                                else -> Unit
                            }
                        }.build()
                        exo.setMediaItem(item)
                        exo.prepare()
                        exo.playWhenReady = true
                    }
            }

            if (!rendered.await(90, TimeUnit.SECONDS)) {
                "FIRST_FRAME_FAILED ${error ?: "timeout"}"
            } else if (error != null) {
                "PLAYER_ERROR $error"
            } else {
                "FIRST_FRAME_OK"
            }
        } finally {
            scenario.onActivity { activity ->
                player?.release()
                surfaceView?.let { activity.findViewById<ViewGroup>(android.R.id.content)?.removeView(it) }
            }
            scenario.close()
        }
    }
}
