package com.kakaanime.app.provider

import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kakaanime.provider.AnimeProvider
import com.kakaanime.provider.ProviderStream
import com.kakaanime.provider.SamehadakuProvider
import com.kakaanime.provider.StreamType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ProviderBatchE2ETest {
    @Test
    fun samehadakuFirstFrameDiagnostic() = runBlocking {
        val provider = SamehadakuProvider(null)
        val label = "Samehadaku-native"
        val result = try { runProvider(label, provider) } catch (t: Throwable) { "ERROR ${t.javaClass.simpleName}: ${t.message}" }
        println("========== KAKAANIME SAMEHADAKU FIRST FRAME ==========")
        println("$label = $result")
        println("=======================================================")
        assertTrue("Diagnostic completed; inspect result above", result.isNotBlank())
    }

    private suspend fun runProvider(label: String, provider: AnimeProvider): String {
        val results = provider.search("One Piece")
        if (results.isEmpty()) return "SEARCH_FAILED"
        val anime = results.firstOrNull { it.title.equals("One Piece", true) || it.id.contains("one-piece", true) } ?: results.first()
        if (provider.getAnime(anime.id) == null) return "DETAIL_FAILED id=${anime.id}"
        val episodes = provider.getEpisodes(anime.id)
        val episode = episodes.firstOrNull { it.number == 1 } ?: return "EPISODE_1_FAILED count=${episodes.size}"
        val streams = provider.getStreams(anime.id, episode.number)
        if (streams.isEmpty()) return "STREAM_FAILED"
        val selected = streams.filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
            .sortedByDescending { score(it.type) }.firstOrNull() ?: return "HTTP_STREAM_FAILED"
        println("[KAKA-CCTV][$label] STREAM type=${selected.type} quality=${selected.quality ?: "unknown"} url=${selected.url.take(180)}")
        if (selected.type == StreamType.UNKNOWN) return "UNKNOWN_STREAM_TYPE"
        return renderFirstFrame(label, selected)
    }

    private fun score(type: StreamType) = when (type) {
        StreamType.HLS, StreamType.DASH -> 3
        StreamType.MP4 -> 2
        StreamType.UNKNOWN -> 0
    }

    private fun renderFirstFrame(label: String, stream: ProviderStream): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val rendered = CountDownLatch(1)
        var error: String? = null
        var player: ExoPlayer? = null
        val thread = android.os.HandlerThread("KakaAnime-$label-E2E").apply { start() }
        val handler = android.os.Handler(thread.looper)
        try {
            handler.post {
                val http = DefaultHttpDataSource.Factory().setUserAgent("KakaAnime/0.1").setDefaultRequestProperties(stream.headers)
                player = ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(context, http))).build().also { exo ->
                    exo.addAnalyticsListener(object : AnalyticsListener {
                        override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
                            println("[KAKA-CCTV][$label] FIRST_FRAME_RENDERED")
                            rendered.countDown()
                        }
                        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, playbackException: androidx.media3.common.PlaybackException) {
                            error = playbackException.errorCodeName + " " + (playbackException.message ?: "")
                            println("[KAKA-CCTV][$label] PLAYER_ERROR $error")
                        }
                        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
                            println("[KAKA-CCTV][$label] STATE=$state")
                        }
                    })
                    val item = MediaItem.Builder().setUri(stream.url).apply {
                        when (stream.type) { StreamType.HLS -> setMimeType(MimeTypes.APPLICATION_M3U8); StreamType.DASH -> setMimeType(MimeTypes.APPLICATION_MPD); else -> Unit }
                    }.build()
                    exo.setMediaItem(item); exo.prepare(); exo.playWhenReady = true
                }
            }
            if (!rendered.await(90, TimeUnit.SECONDS)) return "FIRST_FRAME_FAILED ${error ?: "timeout"}"
            return "FIRST_FRAME_OK"
        } finally {
            val released = CountDownLatch(1)
            handler.post { try { player?.release() } finally { released.countDown() } }
            released.await(10, TimeUnit.SECONDS)
            thread.quitSafely(); thread.join(5_000)
        }
    }
}
