package com.kakaanime.app.provider

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * P0 gate for the first real provider.
 *
 * The test deliberately uses OtakudesuProvider directly instead of the smart
 * router so a green result proves that this provider itself can complete the
 * whole path:
 * search -> detail -> episode -> stream -> Media3 -> first rendered frame.
 */
@RunWith(AndroidJUnit4::class)
class OtakudesuProviderE2ETest {

    @Test
    fun onePieceEpisodeOneRendersFirstFrame() = runBlocking {
        val provider = OtakudesuProvider()

        val searchResults = provider.search("One Piece")
        assertFalse("Otakudesu search returned no results", searchResults.isEmpty())

        val anime = searchResults.firstOrNull {
            it.title.contains("One Piece", ignoreCase = true)
        } ?: searchResults.first()

        val detail = provider.getAnime(anime.id)
        assertNotNull("Otakudesu detail resolution failed for ${anime.id}", detail)

        val episodes = provider.getEpisodes(anime.id)
        assertFalse("Otakudesu returned no episodes for ${anime.id}", episodes.isEmpty())

        val episode = episodes.firstOrNull { it.number == 1 } ?: episodes.minBy { it.number }
        val streams = provider.getStreams(anime.id, episode.number)
        assertFalse(
            "Otakudesu returned no streams for episode ${episode.number}",
            streams.isEmpty()
        )

        val stream = streams.firstOrNull { candidate ->
            candidate.url.startsWith("https://") || candidate.url.startsWith("http://")
        }
        assertNotNull("Otakudesu returned no HTTP(S) stream URL", stream)

        val selected = stream!!
        println(
            "E2E_PROVIDER=otakudesu anime=${anime.title} episode=${episode.number} " +
                "type=${selected.type} quality=${selected.quality} url=${selected.url.take(180)}"
        )

        renderFirstFrame(selected)
    }

    private fun renderFirstFrame(stream: ProviderStream) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val rendered = CountDownLatch(1)
        val failed = CountDownLatch(1)
        var failureMessage: String? = null
        var player: ExoPlayer? = null

        val mainThread = HandlerThreadRunner()
        try {
            mainThread.run {
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("KakaAnime/0.1")
                    .setDefaultRequestProperties(stream.headers)

                val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
                val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

                player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
                    .also { exoPlayer ->
                        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                            override fun onRenderedFirstFrame(
                                eventTime: AnalyticsListener.EventTime,
                                output: Any,
                                renderTimeMs: Long
                            ) {
                                rendered.countDown()
                            }

                            override fun onPlayerError(
                                eventTime: AnalyticsListener.EventTime,
                                error: androidx.media3.common.PlaybackException
                            ) {
                                failureMessage = error.message ?: error.errorCodeName
                                failed.countDown()
                            }
                        })

                        exoPlayer.setMediaItem(MediaItem.fromUri(stream.url))
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
            }

            val renderedInTime = rendered.await(90, TimeUnit.SECONDS)
            if (!renderedInTime) {
                assertTrue(
                    "Media3 did not render the first frame within 90s. " +
                        "Playback error=${failureMessage ?: "none reported"}",
                    failed.count == 0L
                )
            }
            assertTrue("Media3 never reached onRenderedFirstFrame", renderedInTime)
        } finally {
            mainThread.run {
                player?.release()
            }
            mainThread.close()
        }
    }

    private class HandlerThreadRunner {
        private val latch = CountDownLatch(1)
        private val thread = Thread {
            Looper.prepare()
            latch.countDown()
            Looper.loop()
        }
        private var handler: android.os.Handler? = null

        init {
            thread.start()
            assertTrue("Media3 test looper failed to start", latch.await(5, TimeUnit.SECONDS))
            handler = android.os.Handler(thread.looper())
        }

        fun run(block: () -> Unit) {
            val done = CountDownLatch(1)
            var thrown: Throwable? = null
            handler!!.post {
                try {
                    block()
                } catch (t: Throwable) {
                    thrown = t
                } finally {
                    done.countDown()
                }
            }
            assertTrue("Media3 main thread task timed out", done.await(10, TimeUnit.SECONDS))
            thrown?.let { throw it }
        }

        fun close() {
            handler?.post { Looper.myLooper()?.quitSafely() }
            thread.join(5_000)
        }

        private fun Thread.looper(): Looper =
            Looper.getMainLooper().let { main ->
                if (Thread.currentThread() === thread) main else LooperHolder.looper(thread)
            }

        private object LooperHolder {
            fun looper(thread: Thread): Looper {
                var looper: Looper? = null
                val ready = CountDownLatch(1)
                val probe = Thread {
                    Looper.prepare()
                    looper = Looper.myLooper()
                    ready.countDown()
                    Looper.loop()
                }
                probe.start()
                ready.await(5, TimeUnit.SECONDS)
                probe.interrupt()
                return looper ?: Looper.getMainLooper()
            }
        }
    }
}
