package com.kakaanime.app.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    introStart: Long = 0L,
    introEnd: Long = 0L,
    outroStart: Long = 0L,
    outroEnd: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val landscape =
        configuration.screenWidthDp > configuration.screenHeightDp

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = false
        }
    }

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying
            delay(300)
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    if (landscape) {
        LandscapePlayer(player, position, duration, playing, modifier)
    } else {
        PortraitPlayer(player, position, duration, playing, modifier)
    }
}

@Composable
private fun LandscapePlayer(
    player: ExoPlayer,
    position: Long,
    duration: Long,
    playing: Boolean,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "KakaAnime",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallButton("1080p") {}
                    SmallButton("1x") {}
                    SmallButton("⛶") {}
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton("↶") {
                    player.seekTo(
                        (player.currentPosition - 10_000L)
                            .coerceAtLeast(0L)
                    )
                }

                Spacer(Modifier.width(28.dp))

                ControlButton(
                    if (playing) "Ⅱ" else "▶",
                    true
                ) {
                    if (player.isPlaying) player.pause()
                    else player.play()
                }

                Spacer(Modifier.width(28.dp))

                ControlButton("↷") {
                    player.seekTo(
                        (player.currentPosition + 10_000L)
                            .coerceAtMost(
                                player.duration.coerceAtLeast(0L)
                            )
                    )
                }
            }

            Column(Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0L) {
                        (position.toFloat() / duration)
                            .coerceIn(0f, 1f)
                    } else 0f,
                    onValueChange = {
                        if (duration > 0L) {
                            player.seekTo((it * duration).toLong())
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatTime(position),
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallButton("AUTO NEXT") {}
                        SmallButton("NEXT ▶") {}
                    }

                    Text(
                        formatTime(duration),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitPlayer(
    player: ExoPlayer,
    position: Long,
    duration: Long,
    playing: Boolean,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ControlButton("↶") {
                    player.seekTo(
                        (player.currentPosition - 10_000L)
                            .coerceAtLeast(0L)
                    )
                }

                Spacer(Modifier.width(20.dp))

                ControlButton(
                    if (playing) "Ⅱ" else "▶",
                    true
                ) {
                    if (player.isPlaying) player.pause()
                    else player.play()
                }

                Spacer(Modifier.width(20.dp))

                ControlButton("↷") {
                    player.seekTo(
                        (player.currentPosition + 10_000L)
                            .coerceAtMost(
                                player.duration.coerceAtLeast(0L)
                            )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "One Piece",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Episode sedang diputar",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallButton("1080p") {}
                SmallButton("1x") {}
                SmallButton("AUTO NEXT") {}
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Episode List",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EpisodeChip("1138", false) {}
                EpisodeChip("1139", false) {}
                EpisodeChip("1140", true) {}
                EpisodeChip("1141", false) {}
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Komentar",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Komentar akan tersedia di versi berikutnya.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ControlButton(
    text: String,
    large: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(if (large) 64.dp else 50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.65f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = Color.White,
                fontSize = if (large) 26.sp else 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SmallButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EpisodeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color.White else Color.DarkGray
    ) {
        Text(
            text,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000L
    val minutes = seconds / 60L
    val remaining = seconds % 60L

    return "%02d:%02d".format(minutes, remaining)
}
