package com.kakaanime.app.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    accent: Color = Color(0xFF4DA3FF),
    autoNext: Boolean = true,
    onAutoNext: () -> Unit = {},
    onBack: () -> Unit = {},
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
            .clickable { if (!locked) visible = !visible }
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
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = Color.White)
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 21.dp)
                )

                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAutoNext, modifier = Modifier.size(44.dp)) {
                        Icon(
                            if (autoNext) Icons.Filled.Autorenew else Icons.Filled.SyncDisabled,
                            if (autoNext) "Auto next aktif" else "Auto next nonaktif",
                            tint = if (autoNext) accent else Color.White
                        )
                    }
                    IconButton(onClick = { locked = !locked }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            if (locked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            if (locked) "Buka kontrol" else "Kunci kontrol",
                            tint = Color.White
                        )
                    }
                }

                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeekAction(Icons.Filled.FastRewind, "10", onSeekBack)
                    Spacer(Modifier.width(20.dp))
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                        Icon(
                            if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            if (state.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    SeekAction(Icons.Filled.FastForward, "10", onSeekForward)
                }

                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (state.duration > 0L) {
                        Slider(
                            value = state.position.toFloat().coerceIn(0f, state.duration.toFloat()),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..state.duration.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.32f)
                            )
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (previousEpisode != null) {
                            EpisodeAction(Icons.Filled.SkipPrevious, previousEpisode, accent, onPreviousEpisode)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${formatPlayerTime(state.position)} / ${formatPlayerTime(state.duration)}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.weight(1f))
                        if (nextEpisode != null) {
                            EpisodeAction(Icons.Filled.SkipNext, nextEpisode, accent, onNextEpisode)
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onSpeed, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Filled.Speed, "Kecepatan", tint = Color.White)
                        }
                        IconButton(onClick = onFullscreen, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Filled.Fullscreen, "Layar penuh", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, "${label} detik", tint = Color.White, modifier = Modifier.size(34.dp))
        }
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun EpisodeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(icon, text, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

private fun formatPlayerTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
