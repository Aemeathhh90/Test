package com.kakaanime.app.player.core

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerController(
    private val core: PlayerCore
) {

    val player: ExoPlayer
        get() = core.player

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState(
                isReady = playbackState == Player.STATE_READY,
                isBuffering = playbackState == Player.STATE_BUFFERING
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = error.message,
                isBuffering = false
            )
        }
    }

    init {
        core.player.addListener(listener)
    }

    fun refreshPosition() {
        val player = core.player

        _state.value = _state.value.copy(
            position = player.currentPosition.coerceAtLeast(0L),
            duration = player.duration.coerceAtLeast(0L),
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L),
            isPlaying = player.isPlaying
        )
    }

    fun play() {
        core.play()
        refreshPosition()
    }

    fun pause() {
        core.pause()
        refreshPosition()
    }

    fun togglePlayPause() {
        core.togglePlayPause()
        refreshPosition()
    }

    fun seekBack() {
        core.seekBack()
        refreshPosition()
    }

    fun seekForward() {
        core.seekForward()
        refreshPosition()
    }

    fun seekTo(position: Long) {
        core.seekTo(position)
        refreshPosition()
    }

    fun setVideo(url: String) {
        core.setVideo(url)
    }

    fun setSpeed(speed: Float) {
        core.setSpeed(speed)
    }

    fun release() {
        core.player.removeListener(listener)
        core.release()
    }

    private fun updateState(
        isReady: Boolean = _state.value.isReady,
        isPlaying: Boolean = _state.value.isPlaying,
        isBuffering: Boolean = _state.value.isBuffering
    ) {
        val player = core.player

        _state.value = _state.value.copy(
            position = player.currentPosition.coerceAtLeast(0L),
            duration = player.duration.coerceAtLeast(0L),
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L),
            isReady = isReady,
            isPlaying = isPlaying,
            isBuffering = isBuffering
        )
    }
}
