package com.kakaanime.app.provider

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@UnstableApi
@RunWith(AndroidJUnit4::class)
class ProviderBatchE2ETest {

    @Test
    fun samehadakuFirstFrameDiagnostic() {
        val provider = SamehadakuProvider(null)
        val anime = provider.search("One Piece").firstOrNull()
            ?: return println("[KAKA-CCTV][Samehadaku-native] SEARCH_FAILED")
        val detail = provider.getAnime(anime.id)
            ?: return println("[KAKA-CCTV][Samehadaku-native] DETAIL_FAILED")
        val episode = provider.getEpisodes(detail.id).firstOrNull { it.number == 7 }
            ?: return println("[KAKA-CCTV][Samehadaku-native] EPISODE_FAILED")
        val streams = provider.getStreams(detail.id, episode.number)
        val stream = streams.firstOrNull()
            ?: return println("[KAKA-CCTV][Samehadaku-native] STREAM_FAILED")

        println("[KAKA-CCTV][Samehadaku-native] STREAM type=${stream.type} quality=${stream.quality} url=${stream.url} headers=${stream.headers}")
        val result = renderFirstFrame(stream)
        println("[KAKA-CCTV][Samehadaku-native] RESULT=$result")
        assertTrue(result.isNotBlank())
    }

    private fun renderFirstFrame(stream: ProviderStream): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val latch = CountDownLatch(1)
        var firstFrame = false
        var error: String? = null
        var state = -1
        val player = ExoPlayer.Builder(context).build()
        val headers = LinkedHashMap(stream.headers)
        headers.putIfAbsent("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36")
        headers.putIfAbsent("Accept", "*/*")
        headers.putIfAbsent("Referer", "https://v2.samehadaku.how/")

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)

        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onRenderedFirstFrame() {
                firstFrame = true
                println("[KAKA-CCTV][Samehadaku-native] FIRST_FRAME_RENDERED")
                latch.countDown()
            }

            override fun onPlayerError(exception: PlaybackException) {
                error = exception.toString()
                println("[KAKA-CCTV][Samehadaku-native] PLAYER_ERROR $exception")
                latch.countDown()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                state = playbackState
                println("[KAKA-CCTV][Samehadaku-native] STATE=$playbackState")
            }
        })

        val mediaItem = MediaItem.Builder()
            .setUri(stream.url)
            .setMimeType(
                when (stream.type) {
                    StreamType.HLS -> MimeTypes.APPLICATION_M3U8
                    StreamType.DASH -> MimeTypes.APPLICATION_MPD
                    else -> null
                }
            )
            .build()

        val source = when (stream.type) {
            StreamType.HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            StreamType.DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }

        return try {
            player.setMediaSource(source)
            player.prepare()
            player.playWhenReady = true
            latch.await(90, TimeUnit.SECONDS)
            when {
                firstFrame -> "FIRST_FRAME_OK"
                error != null -> "PLAYER_ERROR $error"
                else -> "FIRST_FRAME_FAILED timeout state=$state"
            }
        } finally {
            player.release()
        }
    }
}
