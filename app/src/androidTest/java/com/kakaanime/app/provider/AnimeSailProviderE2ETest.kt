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
import com.kakaanime.provider.AnimeSailProvider
import com.kakaanime.provider.ProviderStream
import com.kakaanime.provider.StreamType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** P0 gate for AnimeSail. One Piece episode 1 is the baseline stream case. */
@RunWith(AndroidJUnit4::class)
class AnimeSailProviderE2ETest {

    @Test
    fun onePieceEpisodeOneRendersFirstFrame() = runBlocking {
        val provider = AnimeSailProvider()
        val searchResults = provider.search("One Piece")
        assertFalse("AnimeSail search returned no results", searchResults.isEmpty())

        val anime = searchResults.firstOrNull {
            it.title.trim().equals("One Piece", ignoreCase = true) ||
                it.id.contains("one-piece", ignoreCase = true)
        }
        assertNotNull("AnimeSail search did not return the canonical One Piece result", anime)

        val selectedAnime = anime!!
        assertNotNull("AnimeSail detail resolution failed for ${selectedAnime.id}", provider.getAnime(selectedAnime.id))

        val episodes = provider.getEpisodes(selectedAnime.id)
        assertFalse("AnimeSail returned no episodes for ${selectedAnime.id}", episodes.isEmpty())

        val episode = episodes.firstOrNull { it.number == 1 }
        assertNotNull("AnimeSail episode 1 is not present for ${selectedAnime.id}", episode)

        val selectedEpisode = episode!!
        val streams = provider.getStreams(selectedAnime.id, selectedEpisode.number)
        assertFalse("AnimeSail returned no streams for episode 1", streams.isEmpty())

        streams.forEachIndexed { index, candidate ->
            Log.i(TAG, "E2E_STREAM[$index] provider=${candidate.providerId} type=${candidate.type} quality=${candidate.quality} headers=${candidate.headers.keys} url=${candidate.url.take(240)}")
        }

        val selected = streams
            .filter { it.url.startsWith("https://") || it.url.startsWith("http://") }
            .sortedByDescending { typeScore(it.type) }
            .firstOrNull()
        assertNotNull("AnimeSail returned no HTTP(S) stream URL", selected)
        assertTrue("AnimeSail selected an UNKNOWN stream type", selected!!.type != StreamType.UNKNOWN)

        Log.i(TAG, "E2E_PROVIDER=animesail anime=${selectedAnime.title} episode=1 type=${selected.type} quality=${selected.quality} url=${selected.url.take(240)}")
        renderFirstFrame(selected)
    }

    private fun typeScore(type: StreamType): Int = when (type) {
        StreamType.HLS, StreamType.DASH -> 3
        StreamType.MP4 -> 2
        StreamType.UNKNOWN -> 0
    }

    private fun renderFirstFrame(stream: ProviderStream) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val rendered = CountDownLatch(1)
        val failed = CountDownLatch(1)
        var failureMessage: String? = null
        var failureCode: String? = null
        var failureCause: String? = null
        var player: ExoPlayer? = null
        val playerThread = HandlerThread("AniLab-AnimeSail-E2E").apply { start() }
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
                    player = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build().also { exoPlayer ->
                        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                            override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
                                rendered.countDown()
                            }
                            override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: androidx.media3.common.PlaybackException) {
                                failureMessage = error.message ?: error.errorCodeName
                                failureCode = error.errorCodeName
                                failureCause = error.cause?.let { cause -> generateSequence(cause) { it.cause }.take(4).joinToString(" -> ") { it::class.java.simpleName + ": " + (it.message ?: "") } }
                                Log.e(TAG, "E2E_MEDIA3_ERROR code=${failureCode ?: "unknown"} message=${failureMessage ?: "none"} cause=${failureCause ?: "none"}")
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
                        exoPlayer.setMediaItem(item)
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
                throw AssertionError("Media3 did not render the first frame within 90s. Playback error=${failureMessage ?: "none reported"} code=${failureCode ?: "none"} cause=${failureCause ?: "none"}")
            }
            assertTrue("Media3 playback failed before onRenderedFirstFrame: ${failureMessage ?: "unknown error"} code=${failureCode ?: "unknown"} cause=${failureCause ?: "unknown"}", renderedInTime)
        } finally {
            val released = CountDownLatch(1)
            handler.post { try { player?.release() } finally { released.countDown() } }
            released.await(10, TimeUnit.SECONDS)
            playerThread.quitSafely()
            playerThread.join(5_000)
        }
    }

    private companion object { const val TAG = "AniLab-AnimeSail-E2E" }
}
