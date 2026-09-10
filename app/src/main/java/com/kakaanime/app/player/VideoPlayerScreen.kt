package com.kakaanime.app.player

import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    modifier: Modifier = Modifier,
    onNextEpisode: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val configuration = LocalConfiguration.current

    val isLandscape =
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

    var autoSkipIntro by remember { mutableStateOf(false) }
    var autoSkipOutro by remember { mutableStateOf(false) }

    var speed by remember { mutableStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    var showQualityMenu by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("1080p") }

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying

            val seconds = position / 1000L

            if (
                autoSkipIntro &&
                introStart > 0L &&
                introEnd > introStart &&
                seconds >= introStart &&
                seconds < introEnd
            ) {
                player.seekTo(introEnd * 1000L)
            }

            if (
                autoSkipOutro &&
                outroStart > 0L &&
                outroEnd > outroStart &&
                seconds >= outroStart &&
                seconds < outroEnd
            ) {
                player.seekTo(outroEnd * 1000L)
            }

            delay(300)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    if (isLandscape) {
        LandscapePlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            speed = speed,
            showSpeedMenu = showSpeedMenu,
            autoSkipIntro = autoSkipIntro,
            autoSkipOutro = autoSkipOutro,
            onSpeedMenu = {
                showSpeedMenu = !showSpeedMenu
            },
            onSpeedSelected = {
                speed = it
                player.setPlaybackSpeed(it)
                showSpeedMenu = false
            },
            onAutoSkipIntro = {
                autoSkipIntro = !autoSkipIntro
            },
            onAutoSkipOutro = {
                autoSkipOutro = !autoSkipOutro
            },
            onBackPortrait = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            onNextEpisode = onNextEpisode,
            modifier = modifier
        )
    } else {
        PortraitPlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            quality = quality,
            showQualityMenu = showQualityMenu,
            onQualityMenu = {
                showQualityMenu = !showQualityMenu
            },
            onQualitySelected = {
                quality = it
                showQualityMenu = false
            },
            onLandscape = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            },
            modifier = modifier
        )
    }
}

@Composable
private fun LandscapePlayer(
    player: ExoPlayer,
    position: Long,
    duration: Long,
    playing: Boolean,
    speed: Float,
    showSpeedMenu: Boolean,
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    onSpeedMenu: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onAutoSkipIntro: () -> Unit,
    onAutoSkipOutro: () -> Unit,
    onBackPortrait: () -> Unit,
    onNextEpisode: () -> Unit,
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
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT

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
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    SmallButton("1x") {
                        onSpeedMenu()
                    }

                    SmallButton("AUTO NEXT") {}

                    SmallButton("⛶") {
                        onBackPortrait()
                    }
                }
            }

            if (showSpeedMenu) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(0.5f, 1f, 1.25f, 1.5f, 2f).forEach {
                        SmallButton("${it}x") {
                            onSpeedSelected(it)
                        }
                    }
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
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
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

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallButton(
                        if (autoSkipIntro)
                            "SKIP INTRO: AUTO"
                        else
                            "SKIP INTRO: MANUAL"
                    ) {
                        onAutoSkipIntro()
                    }

                    Spacer(Modifier.width(8.dp))

                    SmallButton(
                        if (autoSkipOutro)
                            "SKIP OUTRO: AUTO"
                        else
                            "SKIP OUTRO: MANUAL"
                    ) {
                        onAutoSkipOutro()
                    }

                    Spacer(Modifier.width(8.dp))

                    SmallButton("NEXT ▶") {
                        onNextEpisode()
                    }
                }

                Slider(
                    value = if (duration > 0L) {
                        (position.toFloat() / duration)
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    },
                    onValueChange = {
                        if (duration > 0L) {
                            player.seekTo(
                                (it * duration).toLong()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
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
    quality: String,
    showQualityMenu: Boolean,
    onQualityMenu: () -> Unit,
    onQualitySelected: (String) -> Unit,
    onLandscape: () -> Unit,
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
                        resizeMode =
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton("↶") {
                    player.seekTo(
                        (player.currentPosition - 10_000L)
                            .coerceAtLeast(0L)
                    )
                }

                Spacer(Modifier.width(18.dp))

                ControlButton(
                    if (playing) "Ⅱ" else "▶",
                    true
                ) {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }

                Spacer(Modifier.width(18.dp))

                ControlButton("↷") {
                    player.seekTo(
                        (player.currentPosition + 10_000L)
                            .coerceAtMost(
                                player.duration.coerceAtLeast(0L)
                            )
                    )
                }

                Spacer(Modifier.width(18.dp))

                ControlButton("⛶") {
                    onLandscape()
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

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatTime(position),
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(Modifier.width(8.dp))

                Slider(
                    value = if (duration > 0L) {
                        (position.toFloat() / duration)
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    },
                    onValueChange = {
                        if (duration > 0L) {
                            player.seekTo(
                                (it * duration).toLong()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    formatTime(duration),
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Quality",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Box {
                SmallButton(quality) {
                    onQualityMenu()
                }

                DropdownMenu(
                    expanded = showQualityMenu,
                    onDismissRequest = onQualityMenu
                ) {
                    listOf("360p", "480p", "720p", "1080p").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                onQualitySelected(it)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallButton("👍 Like") {}
                SmallButton("👎 Dislike") {}
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
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
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
        color = Color.Black.copy(alpha = 0.70f)
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
        color = Color.Black.copy(alpha = 0.75f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
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
                horizontal = 15.dp,
                vertical = 10.dp
            ),
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "%02d:%02d".format(minutes, seconds)
}
