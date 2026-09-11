package com.kakaanime.app.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.player.core.PlayerState
import kotlinx.coroutines.delay

@Composable
fun ReDantotsuPlayerController(
    state: PlayerState,
    title: String = "KakaAnime",
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
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(visible, state.isPlaying) {
        if (visible) {
            delay(3500L)
            visible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                visible = !visible
            }
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.70f)
                            )
                        )
                    )
            ) {

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(18.dp)
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onSeekBack
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastRewind,
                            contentDescription = "Mundur 10 detik",
                            tint = Color.White
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(18.dp)
                    )

                    IconButton(
                        onClick = onPlayPause
                    ) {
                        Icon(
                            imageVector =
                                if (state.isPlaying)
                                    Icons.Filled.Pause
                                else
                                    Icons.Filled.PlayArrow,
                            contentDescription =
                                if (state.isPlaying)
                                    "Pause"
                                else
                                    "Play",
                            tint = Color.White
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(18.dp)
                    )

                    IconButton(
                        onClick = onSeekForward
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Maju 10 detik",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 14.dp
                        )
                ) {

                    if (state.duration > 0L) {

                        Slider(
                            value =
                                state.position
                                    .toFloat()
                                    .coerceIn(
                                        0f,
                                        state.duration.toFloat()
                                    ),
                            onValueChange = {
                                onSeekTo(it.toLong())
                            },
                            valueRange =
                                0f..state.duration.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor =
                                    Color.White.copy(alpha = 0.35f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (previousEpisode != null) {
                            IconButton(
                                onClick = onPreviousEpisode
                            ) {
                                Icon(
                                    Icons.Filled.SkipPrevious,
                                    contentDescription = "Episode sebelumnya",
                                    tint = Color.White
                                )
                            }

                            Text(
                                text = previousEpisode,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = formatPlayerTime(state.position),
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Text(
                            text = " / ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )

                        Text(
                            text = formatPlayerTime(state.duration),
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        if (nextEpisode != null) {
                            Text(
                                text = nextEpisode,
                                color = Color.White,
                                fontSize = 12.sp
                            )

                            IconButton(
                                onClick = onNextEpisode
                            ) {
                                Icon(
                                    Icons.Filled.SkipNext,
                                    contentDescription = "Episode berikutnya",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPlayerTime(milliseconds: Long): String {
    if (milliseconds <= 0L) return "00:00"

    val totalSeconds = milliseconds / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
