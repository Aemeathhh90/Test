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
import com.kakaanime.provider.NativeHtmlProvider
import com.kakaanime.provider.OtakudesuProvider
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
    fun fourNativeProvidersRunToFirstFrameOrExactFailurePoint() = runBlocking {
        val providers = listOf(
            "Samehadaku-native" to SamehadakuProvider(null),
            "Otakudesu-native" to OtakudesuProvider(null),
            "Anoboy-native-html" to NativeHtmlProvider("anoboy", "Anoboy", 60, "https://anoboy.xyz", null),
            "Kuronime-native-html" to NativeHtmlProvider("kuronime", "Kuronime", 80, "https://kuronime.net", null)
        )
        val summary = mutableListOf<String>()
        for ((label, provider) in providers) {
            try { summary += "$label = ${runProvider(label, provider)}" }
            catch (t: Throwable) { summary += "$label = ERROR ${t.javaClass.simpleName}: ${t.message}" }
        }
        println("========== KAKAANIME NATIVE PROVIDER BATCH ==========")
        summary.forEach(::println)
        println("=======================================================")
        assertTrue("Batch completed; inspect provider summary above", summary.size == providers.size)
    }

    private suspend fun runProvider(label: String, provider: AnimeProvider): String {
        println("[$label] SEARCH")
        val results = provider.search("One Piece")
        if (results.isEmpty()) return "SEARCH_FAILED"
        println("[$label] SEARCH_OK count=${results.size}")
        val anime = results.firstOrNull { it.title.equals("One Piece", true) || it.id.contains("one-piece", true) } ?: results.first()
        if (provider.getAnime(anime.id) == null) return "DETAIL_FAILED id=${anime.id}"
        println("[$label] DETAIL_OK id=${anime.id}")
        val episodes = provider.getEpisodes(anime.id)
        val episode = episodes.firstOrNull { it.number == 1 } ?: return "EPISODE_1_FAILED count=${episodes.size}"
        println("[$label] EPISODE_OK count=${episodes.size}")
        val streams = provider.getStreams(anime.id, episode.number)
        if (streams.isEmpty()) return "STREAM_FAILED"
        println("[$label] STREAM_OK count=${streams.size}")
        val selected = streams.filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
            .sortedByDescending { score(it.type) }.firstOrNull() ?: return "HTTP_STREAM_FAILED"
        if (selected.type == StreamType.UNKNOWN) return "UNKNOWN_STREAM_TYPE url=${selected.url.take(120)}"
        renderFirstFrame(label, selected)
        return "FIRST_FRAME_OK"
    }

    private fun score(type: StreamType) = when (type) {
        StreamType.HLS, StreamType.DASH -> 3
        StreamType.MP4 -> 2
        StreamType.UNKNOWN -> 0
    }

    private fun renderFirstFrame(label: String, stream: ProviderStream) {
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
                        override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) = rendered.countDown()
                        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, playbackException: androidx.media3.common.PlaybackException) { error = playbackException.errorCodeName + " " + (playbackException.message ?: "") }
                    })
                    val item = MediaItem.Builder().setUri(stream.url).apply {
                        when (stream.type) { StreamType.HLS -> setMimeType(MimeTypes.APPLICATION_M3U8); StreamType.DASH -> setMimeType(MimeTypes.APPLICATION_MPD); else -> Unit }
                    }.build()
                    exo.setMediaItem(item); exo.prepare(); exo.playWhenReady = true
                }
            }
            if (!rendered.await(90, TimeUnit.SECONDS)) throw AssertionError("$label FIRST_FRAME_FAILED ${error ?: "timeout"}")
        } finally {
            val released = CountDownLatch(1)
            handler.post { try { player?.release() } finally { released.countDown() } }
            released.await(10, TimeUnit.SECONDS)
            thread.quitSafely(); thread.join(5_000)
        }
    }
}
