package com.kakaanime.app.player.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay

@Composable
fun rememberPlayerController(
    context: Context
): PlayerController {
    val controller = remember(context) {
        PlayerController(PlayerCore(context))
    }

    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}

@Composable
fun rememberPlayerState(
    controller: PlayerController
): PlayerState {
    val state by controller.state.collectAsState()

    LaunchedEffect(controller) {
        while (true) {
            controller.refreshPosition()
            delay(250L)
        }
    }

    return state
}
