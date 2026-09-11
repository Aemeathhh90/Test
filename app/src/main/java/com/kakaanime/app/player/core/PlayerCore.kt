package com.kakaanime.app.player.core

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class PlayerCore(
    context: Context
) {

    val player: ExoPlayer =
        ExoPlayer.Builder(context).build()

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
            isReady =
                playbackState == Player.STATE_READY
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
        hasError = false
        errorMessage = null
        isReady = false

        player.setMediaItem(
            MediaItem.fromUri(url)
        )

        player.prepare()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekBack(milliseconds: Long = 10_000L) {
        player.seekTo(
            (player.currentPosition - milliseconds)
                .coerceAtLeast(0L)
        )
    }

    fun seekForward(milliseconds: Long = 10_000L) {
        player.seekTo(
            (player.currentPosition + milliseconds)
                .coerceAtMost(
                    player.duration.coerceAtLeast(0L)
                )
        )
    }

    fun seekTo(position: Long) {
        player.seekTo(position.coerceAtLeast(0L))
    }

    fun setSpeed(speed: Float) {
        player.setPlaybackParameters(
            PlaybackParameters(speed)
        )
    }

    fun release() {
        player.removeListener(listener)
        player.release()
    }
}
