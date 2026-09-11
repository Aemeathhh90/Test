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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Strict gate: Samehadaku is green only after Media3 renders a real first frame. */
@RunWith(AndroidJUnit4::class)
class SamehadakuPlaybackE2eTest {

    @Test
    fun samehadaku_resolves_and_reaches_first_frame() = runBlocking {
        val provider = SamehadakuProvider()
        val searchResults = provider.search("One Piece")
        assertFalse("Samehadaku returned no search results", searchResults.isEmpty())

        val anime = searchResults.first()
        val episodes = provider.getEpisodes(anime.id)
        assertFalse("Samehadaku returned no episodes for ${anime.title}", episodes.isEmpty())

        val episode = episodes.minByOrNull { it.number }
        assertNotNull("Samehadaku returned no selectable episode", episode)
        episode ?: return@runBlocking

        val rawStreams = provider.getStreams(anime.id, episode.number)
        assertFalse("Samehadaku returned no raw streams for episode ${episode.number}", rawStreams.isEmpty())

        val normalized = StreamNormalizer.normalize(rawStreams)
        assertFalse("Samehadaku returned no native HLS/DASH/MP4 streams", normalized.isEmpty())

        val validated = StreamValidator().validate(normalized)
        assertFalse("Samehadaku streams did not pass runtime validation", validated.isEmpty())

        val stream = StreamSelector.best(validated)
        assertNotNull("Samehadaku has validated streams but no selectable best stream", stream)
        stream ?: return@runBlocking

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
                "Samehadaku stream did not render a first frame within 60s" +
                    (failure[0]?.let { ": ${it.errorCodeName}" } ?: ""),
                firstFrame.await(60, TimeUnit.SECONDS) && failure[0] == null
            )
        } finally {
            withContext(Dispatchers.Main) { player.release() }
        }
    }
}
