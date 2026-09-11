package com.kakaanime.app.provider

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Strict runtime gate for every provider currently registered in ProviderFactory.
 * A provider is only eligible for green status after this flow reaches a real
 * Media3 rendered first frame: Search -> Detail/Episodes -> Streams -> Normalize
 * -> HTTP validation -> Select -> Media3 playback.
 *
 * This intentionally tests the current runtime registry instead of maintaining
 * a second hand-written provider list that can drift from ProviderFactory.
 */
@RunWith(AndroidJUnit4::class)
class AllProvidersPlaybackE2eTest {

    @Test
    fun every_registered_provider_reaches_first_frame() = runBlocking {
        val failures = mutableListOf<String>()
        val providers = ProviderFactory.createRegistry().all()

        assertTrue("ProviderFactory registered no providers", providers.isNotEmpty())

        for (provider in providers) {
            try {
                // Keep one unhealthy/stalled provider from consuming the entire batch.
                withTimeout(45_000) {
                    exerciseProvider(provider)
                }
            } catch (t: Throwable) {
                failures += "${provider.id}: ${t.message ?: t::class.java.simpleName}"
            }
        }

        assertTrue(
            "Providers that failed strict playback E2E:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }

    private suspend fun exerciseProvider(provider: AnimeProvider) {
        val searchResults = provider.search("One Piece")
        assertTrue("no search results", searchResults.isNotEmpty())

        val anime = searchResults.first()
        val detail = provider.getAnime(anime.id)
        assertTrue("detail lookup failed for ${anime.id}", detail != null)

        val episodes = provider.getEpisodes(anime.id)
        assertTrue("no episodes for ${anime.title}", episodes.isNotEmpty())

        val episode = episodes.minByOrNull { it.number }
        assertTrue("no selectable episode", episode != null)
        episode ?: return

        val rawStreams = provider.getStreams(anime.id, episode.number)
        assertTrue("no raw streams for episode ${episode.number}", rawStreams.isNotEmpty())

        val normalized = StreamNormalizer.normalize(rawStreams)
        assertTrue("no native HLS/DASH/MP4 streams", normalized.isNotEmpty())

        val validated = StreamValidator().validate(normalized)
        assertTrue("no streams passed runtime validation", validated.isNotEmpty())

        val stream = StreamSelector.best(validated)
        assertTrue("no selectable validated stream", stream != null)
        stream ?: return

        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstFrame = CountDownLatch(1)
        val failure = arrayOfNulls<PlaybackException>(1)

        val player = withContext(Dispatchers.Main) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(
                        DefaultHttpDataSource.Factory()
                            .setUserAgent("KakaAnime/0.1")
                            .setDefaultRequestProperties(stream.headers)
                    )
                )
                .build()
                .also { exo ->
                    exo.addListener(object : Player.Listener {
                        override fun onRenderedFirstFrame() = firstFrame.countDown()

                        override fun onPlayerError(error: PlaybackException) {
                            failure[0] = error
                            firstFrame.countDown()
                        }
                    })

                    val mediaItem = MediaItem.Builder()
                        .setUri(stream.url)
                        .apply {
                            when (stream.type) {
                                StreamType.HLS -> setMimeType(MimeTypes.APPLICATION_M3U8)
                                StreamType.DASH -> setMimeType(MimeTypes.APPLICATION_MPD)
                                StreamType.MP4 -> setMimeType(MimeTypes.VIDEO_MP4)
                                StreamType.UNKNOWN -> Unit
                            }
                        }
                        .build()

                    exo.setMediaItem(mediaItem)
                    exo.prepare()
                    exo.playWhenReady = true
                }
        }

        try {
            assertTrue(
                "first frame not rendered within 25s" +
                    (failure[0]?.let { ": ${it.errorCodeName}" } ?: ""),
                firstFrame.await(25, TimeUnit.SECONDS) && failure[0] == null
            )
        } finally {
            withContext(Dispatchers.Main) { player.release() }
        }
    }
}
