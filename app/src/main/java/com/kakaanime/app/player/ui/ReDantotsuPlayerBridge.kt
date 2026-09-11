package com.kakaanime.app.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kakaanime.app.player.core.PlayerState

@Composable
fun ReDantotsuPlayerBridge(
    state: PlayerState,
    previousEpisode: String? = null,
    nextEpisode: String? = null,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReDantotsuPlayerController(
        state = state,
        title = "KakaAnime",
        previousEpisode = previousEpisode,
        nextEpisode = nextEpisode,
        onPlayPause = onPlayPause,
        onSeekBack = onSeekBack,
        onSeekForward = onSeekForward,
        onSeekTo = onSeekTo,
        onPreviousEpisode = onPreviousEpisode,
        onNextEpisode = onNextEpisode,
        modifier = modifier
    )
}
