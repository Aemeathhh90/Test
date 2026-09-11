package com.kakaanime.app.player.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun rememberPlayerControlsState(
    autoHideMillis: Long = 3500L
): PlayerControlsState {

    val controls = remember {
        PlayerControlsState()
    }

    LaunchedEffect(
        controls,
        controls.resetKey,
        autoHideMillis
    ) {
        if (controls.isVisible) {
            delay(autoHideMillis)
            controls.hide()
        }
    }

    return controls
}
