package com.kakaanime.app.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
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
        modifier = modifier.fillMaxSize().clickable {
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
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.58f), Color.Transparent, Color.Black.copy(alpha = 0.78f))
                    )
                )
            ) {
                GlassAction(Icons.Filled.ArrowBack, "Kembali", accent, Modifier.align(Alignment.TopStart).padding(14.dp)) {}
                androidx.compose.material3.Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 21.dp))
                GlassAction(Icons.Filled.Lock, if (locked) "Buka kontrol" else "Kunci kontrol", accent, Modifier.align(Alignment.TopEnd).padding(14.dp)) { locked = !locked }

                Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    SeekAction(Icons.Filled.FastRewind, "10", accent, onSeekBack)
                    Spacer(Modifier.width(20.dp))
                    Box(Modifier.size(72.dp).clip(CircleShape).background(accent.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
                        IconButton(onClick = onPlayPause) {
                            Icon(if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (state.isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(38.dp))
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    SeekAction(Icons.Filled.FastForward, "10", accent, onSeekForward)
                }

                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    if (state.duration > 0L) {
                        Slider(
                            value = state.position.toFloat().coerceIn(0f, state.duration.toFloat()),
                            onValueChange = { onSeekTo(it.toLong()) },
                            valueRange = 0f..state.duration.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            thumb = {
                                Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
                            },
                            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent, inactiveTrackColor = Color.White.copy(alpha = 0.32f))
                        )
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (previousEpisode != null) EpisodeAction(Icons.Filled.SkipPrevious, previousEpisode, accent, onPreviousEpisode)
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.Text("${formatPlayerTime(state.position)} / ${formatPlayerTime(state.duration)}", color = Color.White, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        if (nextEpisode != null) EpisodeAction(Icons.Filled.SkipNext, nextEpisode, accent, onNextEpisode)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        GlassAction(Icons.Filled.Speed, "Kecepatan", accent, Modifier.size(44.dp), onSpeed)
                        Spacer(Modifier.width(4.dp))
                        GlassAction(Icons.Filled.Fullscreen, "Layar penuh", accent, Modifier.size(44.dp), onFullscreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassAction(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(icon, description, tint = Color.White)
    }
}

@Composable
private fun SeekAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, accent: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, "${label} detik", tint = Color.White, modifier = Modifier.size(34.dp)) }
        androidx.compose.material3.Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun EpisodeAction(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, accent: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp)) {
        Icon(icon, text, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        androidx.compose.material3.Text(text, color = Color.White, fontSize = 11.sp)
    }
}

private fun formatPlayerTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
