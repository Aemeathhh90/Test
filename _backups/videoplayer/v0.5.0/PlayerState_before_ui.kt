package com.kakaanime.app.player.core

data class PlayerState(
    val position: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)
