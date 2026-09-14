package com.kakaanime.app.player.core

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kakaanime.app.provider.NormalizedStream
import com.kakaanime.app.provider.StreamMetadataCache
import com.kakaanime.app.provider.StreamType

class PlayerCore(
    context: Context
) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    var isReady: Boolean = false
        private set
    var isPlaying: Boolean = false
        private set
    var hasError: Boolean = false
        private set
    var errorMessage: String? = null
        private set

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            isReady = playbackState == Player.STATE_READY
        }
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
        }
        override fun onPlayerError(error: PlaybackException) {
            hasError = true
            errorMessage = error.message
        }
    }

    init {
        player.addListener(listener)
    }

    fun setVideo(url: String) {
        val stream = StreamMetadataCache.find(url)
            ?: NormalizedStream(providerId = "", url = url)
        setVideo(stream)
    }

    fun setVideo(stream: NormalizedStream) {
        hasError = false
        errorMessage = null
        isReady = false

        val mediaItem = MediaItem.Builder()
            .setUri(stream.url)
            .apply { mimeTypeFor(stream.type)?.let(::setMimeType) }
            .build()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .apply {
                if (stream.headers.isNotEmpty()) {
                    setDefaultRequestProperties(stream.headers)
                }
            }
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
    }

    private fun mimeTypeFor(type: StreamType): String? = when (type) {
        StreamType.HLS -> MimeTypes.APPLICATION_M3U8
        StreamType.DASH -> MimeTypes.APPLICATION_MPD
        StreamType.MP4 -> MimeTypes.VIDEO_MP4
        StreamType.UNKNOWN -> null
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun togglePlayPause() { if (player.isPlaying) pause() else play() }
    fun seekBack(milliseconds: Long = 10_000L) = player.seekTo((player.currentPosition - milliseconds).coerceAtLeast(0L))
    fun seekForward(milliseconds: Long = 10_000L) = player.seekTo((player.currentPosition + milliseconds).coerceAtMost(player.duration.coerceAtLeast(0L)))
    fun seekTo(position: Long) = player.seekTo(position.coerceAtLeast(0L))
    fun setSpeed(speed: Float) = player.setPlaybackParameters(PlaybackParameters(speed))
    fun release() { player.removeListener(listener); player.release() }
}
