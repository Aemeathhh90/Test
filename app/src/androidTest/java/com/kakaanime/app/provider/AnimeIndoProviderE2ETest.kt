package com.kakaanime.app.provider

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kakaanime.provider.ProviderStream
import com.kakaanime.provider.RemoteSourceProvider
import com.kakaanime.provider.StreamType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** P0 gate for AnimeIndo. One Piece episode 1 is the baseline case. */
@RunWith(AndroidJUnit4::class)
class AnimeIndoProviderE2ETest {
    @Test
    fun onePieceEpisodeOneRendersFirstFrame() = runBlocking {
        val provider = RemoteSourceProvider("animeindo", "AnimeIndo", 40, "animeindo")
        val results = provider.search("One Piece")
        assertFalse("AnimeIndo search returned no results", results.isEmpty())
        val anime = results.firstOrNull {
            it.title.trim().equals("One Piece", true) || it.id.contains("one-piece", true)
        }
        assertNotNull("AnimeIndo search did not return canonical One Piece", anime)

        val selectedAnime = anime!!
        assertNotNull("AnimeIndo detail resolution failed for ${selectedAnime.id}", provider.getAnime(selectedAnime.id))
        val episodes = provider.getEpisodes(selectedAnime.id)
        assertFalse("AnimeIndo returned no episodes for ${selectedAnime.id}", episodes.isEmpty())
        val episode = episodes.firstOrNull { it.number == 1 }
        assertNotNull("AnimeIndo episode 1 is not present for ${selectedAnime.id}", episode)

        val streams = provider.getStreams(selectedAnime.id, episode!!.number)
        assertFalse("AnimeIndo returned no streams for episode 1", streams.isEmpty())
        streams.forEachIndexed { index, stream ->
            Log.i(TAG, "E2E_STREAM[$index] provider=${stream.providerId} type=${stream.type} quality=${stream.quality} headers=${stream.headers.keys} url=${stream.url.take(240)}")
        }

        val selected = streams
            .filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
            .sortedByDescending { typeScore(it.type) }
            .firstOrNull()
        assertNotNull("AnimeIndo returned no HTTP(S) stream URL", selected)
        assertTrue("AnimeIndo selected an UNKNOWN stream type", selected!!.type != StreamType.UNKNOWN)
        renderFirstFrame(selected)
    }

    private fun typeScore(type: StreamType) = when (type) {
        StreamType.HLS, StreamType.DASH -> 3
        StreamType.MP4 -> 2
        StreamType.UNKNOWN -> 0
    }

    private fun renderFirstFrame(stream: ProviderStream) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val rendered = CountDownLatch(1)
        val failed = CountDownLatch(1)
        var errorMessage: String? = null
        var errorCode: String? = null
        var player: ExoPlayer? = null
        val thread = HandlerThread("KakaAnime-AnimeIndo-E2E").apply { start() }
        val handler = Handler(thread.looper)
        try {
            val created = CountDownLatch(1)
            handler.post {
                try {
                    val http = DefaultHttpDataSource.Factory()
                        .setUserAgent("KakaAnime/0.1")
                        .setDefaultRequestProperties(stream.headers)
                    player = ExoPlayer.Builder(context)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(context, http)))
                        .build().also { exo ->
                            exo.addAnalyticsListener(object : AnalyticsListener {
                                override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) = rendered.countDown()
                                override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: androidx.media3.common.PlaybackException) {
                                    errorMessage = error.message ?: error.errorCodeName
                                    errorCode = error.errorCodeName
                                    Log.e(TAG, "E2E_MEDIA3_ERROR code=$errorCode message=$errorMessage", error)
                                    failed.countDown()
                                }
                            })
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
                } finally { created.countDown() }
            }
            assertTrue("Media3 player setup timed out", created.await(10, TimeUnit.SECONDS))
            val ok = rendered.await(90, TimeUnit.SECONDS)
            if (!ok && failed.count == 0L) throw AssertionError("Media3 first frame timeout: ${errorMessage ?: "no error"}")
            assertTrue("Media3 playback failed: ${errorCode ?: "unknown"} ${errorMessage ?: ""}", ok)
        } finally {
            val released = CountDownLatch(1)
            handler.post { try { player?.release() } finally { released.countDown() } }
            released.await(10, TimeUnit.SECONDS)
            thread.quitSafely()
            thread.join(5_000)
        }
    }

    private companion object { const val TAG = "KakaAnime-AnimeIndo-E2E" }
}
