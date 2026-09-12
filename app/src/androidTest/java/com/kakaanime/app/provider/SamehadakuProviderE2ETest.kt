package com.kakaanime.app.provider

import android.os.Handler
import android.os.HandlerThread
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

/** P0 gate for Samehadaku. Episode 7 is the first fixed regression case. */
@RunWith(AndroidJUnit4::class)
class SamehadakuProviderE2ETest {

    @Test
    fun onePieceEpisodeSevenRendersFirstFrame() = runBlocking {
        val provider = SamehadakuProvider()

        val searchResults = provider.search("One Piece")
        assertFalse("Samehadaku search returned no results", searchResults.isEmpty())

        // Search may return titles such as "One Piece Heroines" before the
        // canonical series. Do not let a broad contains() match select the wrong anime.
        val anime = searchResults.firstOrNull {
            it.title.trim().equals("One Piece", ignoreCase = true) ||
                it.id.trimEnd('/').endsWith("/anime/one-piece", ignoreCase = true)
        }
        assertNotNull("Samehadaku search did not return the canonical One Piece result", anime)

        val selectedAnime = anime!!
        val detail = provider.getAnime(selectedAnime.id)
        assertNotNull("Samehadaku detail resolution failed for ${selectedAnime.id}", detail)

        val episodes = provider.getEpisodes(selectedAnime.id)
        assertFalse("Samehadaku returned no episodes for ${selectedAnime.id}", episodes.isEmpty())

        val episode = episodes.firstOrNull { it.number == 7 }
        assertNotNull("Samehadaku episode 7 is not present for ${selectedAnime.id}", episode)

        val selectedEpisode = episode!!
        val streams = provider.getStreams(selectedAnime.id, selectedEpisode.number)
        assertFalse(
            "Samehadaku returned no streams for episode ${selectedEpisode.number}",
            streams.isEmpty()
        )

        val stream = streams.firstOrNull {
            it.url.startsWith("https://") || it.url.startsWith("http://")
        }
        assertNotNull("Samehadaku returned no HTTP(S) stream URL", stream)

        val selected = stream!!
        println(
            "E2E_PROVIDER=samehadaku anime=${selectedAnime.title} episode=${selectedEpisode.number} " +
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

        val playerThread = HandlerThread("AniLab-Samehadaku-E2E").apply { start() }
        val handler = Handler(playerThread.looper)

        try {
            val created = CountDownLatch(1)
            handler.post {
                try {
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
                } finally {
                    created.countDown()
                }
            }

            assertTrue("Media3 player setup timed out", created.await(10, TimeUnit.SECONDS))
            val renderedInTime = rendered.await(90, TimeUnit.SECONDS)
            if (!renderedInTime && failed.count == 0L) {
                throw AssertionError(
                    "Media3 did not render the first frame within 90s. " +
                        "Playback error=${failureMessage ?: "none reported"}"
                )
            }
            assertTrue(
                "Media3 playback failed before onRenderedFirstFrame: ${failureMessage ?: "unknown error"}",
                renderedInTime
            )
        } finally {
            val released = CountDownLatch(1)
            handler.post {
                try {
                    player?.release()
                } finally {
                    released.countDown()
                }
            }
            released.await(10, TimeUnit.SECONDS)
            playerThread.quitSafely()
            playerThread.join(5_000)
        }
    }
}
