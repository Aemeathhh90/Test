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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.draw.clip
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
    accent: Color = Color(0xFF4DA3FF),
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onFullscreen: () -> Unit = {},
    onSpeed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }

    LaunchedEffect(visible, state.isPlaying, locked) {
        if (visible && !locked) {
            delay(3500L)
            visible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (!locked) visible = !visible
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
                                Color.Black.copy(alpha = 0.58f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
            ) {
                GlassAction(
                    icon = Icons.Filled.ArrowBack,
                    description = "Kembali",
                    accent = accent,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    onClick = { }
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 21.dp)
                )

                GlassAction(
                    icon = if (locked) Icons.Filled.Lock else Icons.Filled.Lock,
                    description = if (locked) "Buka kontrol" else "Kunci kontrol",
                    accent = accent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    onClick = { locked = !locked }
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeekAction(
                        icon = Icons.Filled.FastRewind,
                        label = "10",
                        accent = accent,
                        onClick = onSeekBack
                    )

                    Spacer(Modifier.width(20.dp))

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onPlayPause) {
                            Icon(
                                imageVector = if (state.isPlaying)
                                    Icons.Filled.Pause
                                else
                                    Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(20.dp))

                    SeekAction(
                        icon = Icons.Filled.FastForward,
                        label = "10",
                        accent = accent,
                        onClick = onSeekForward
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (state.duration > 0L) {
                        Slider(
                            value = state.position
                                .toFloat()
                                .coerceIn(0f, state.duration.toFloat()),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..state.duration.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = accent,
                                activeTrackColor = accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.32f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (previousEpisode != null) {
                            EpisodeAction(
                                icon = Icons.Filled.SkipPrevious,
                                text = previousEpisode,
                                accent = accent,
                                onClick = onPreviousEpisode
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = "${formatPlayerTime(state.position)} / ${formatPlayerTime(state.duration)}",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.weight(1f))

                        if (nextEpisode != null) {
                            EpisodeAction(
                                icon = Icons.Filled.SkipNext,
                                text = nextEpisode,
                                accent = accent,
                                reverse = true,
                                onClick = onNextEpisode
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassAction(
                            icon = Icons.Filled.Speed,
                            description = "Kecepatan",
                            accent = accent,
                            onClick = onSpeed
                        )

                        Spacer(Modifier.width(6.dp))

                        GlassAction(
                            icon = Icons.Filled.Fullscreen,
                            description = "Fullscreen",
                            accent = accent,
                            onClick = onFullscreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.10f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White
        )
    }
}

@Composable
private fun SeekAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = "$label detik",
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 7.dp)
        )
    }
}

@Composable
private fun EpisodeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accent: Color,
    reverse: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (!reverse) {
            IconButton(onClick = onClick) {
                Icon(icon, contentDescription = text, tint = Color.White)
            }
            Text(text, color = Color.White, fontSize = 11.sp)
        } else {
            Text(text, color = Color.White, fontSize = 11.sp)
            IconButton(onClick = onClick) {
                Icon(icon, contentDescription = text, tint = Color.White)
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
