package com.kakaanime.app.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    introStart: Long = 0L,
    introEnd: Long = 0L,
    outroStart: Long = 0L,
    outroEnd: Long = 0L,
    modifier: Modifier = Modifier,
    onNextEpisode: () -> Unit = {},
    isPremium: Boolean = false
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val landscape = configuration.screenWidthDp > configuration.screenHeightDp

    val colors = MaterialTheme.colorScheme
    val appBackground = colors.background
    val controlColor = colors.surfaceVariant
    val accent = colors.primary

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

    var speed by remember { mutableFloatStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    var autoNext by remember { mutableStateOf(true) }
    var autoSkipIntro by remember { mutableStateOf(false) }
    var autoSkipOutro by remember { mutableStateOf(false) }

    var selectedResolution by remember { mutableStateOf("720p") }
    var showResolutionMenu by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        while (true) {
            position = max(0L, player.currentPosition)
            duration = max(0L, player.duration)
            playing = player.isPlaying

            val second = position / 1000L

            if (
                autoSkipIntro &&
                introStart > 0L &&
                introEnd > introStart &&
                second >= introStart &&
                second < introEnd
            ) {
                player.seekTo(introEnd * 1000L)
            }

            if (
                autoSkipOutro &&
                outroStart > 0L &&
                outroEnd > outroStart &&
                second >= outroStart &&
                second < outroEnd
            ) {
                player.seekTo(outroEnd * 1000L)
            }

            delay(300)
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = {
                Text(
                    "1080p Premium",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Resolusi 1080p hanya tersedia untuk pengguna Premium.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showPremiumDialog = false }
                ) {
                    Text("Berlangganan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPremiumDialog = false }
                ) {
                    Text("Nanti")
                }
            }
        )
    }

    if (landscape) {
        LandscapePlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            speed = speed,
            showSpeedMenu = showSpeedMenu,
            autoNext = autoNext,
            autoSkipIntro = autoSkipIntro,
            autoSkipOutro = autoSkipOutro,
            controlColor = controlColor,
            accent = accent,
            appBackground = appBackground,
            onSpeedClick = {
                showSpeedMenu = !showSpeedMenu
            },
            onSpeedSelected = {
                speed = it
                player.setPlaybackParameters(PlaybackParameters(it))
                showSpeedMenu = false
            },
            onAutoNext = {
                autoNext = !autoNext
            },
            onAutoSkipIntro = {
                autoSkipIntro = !autoSkipIntro
            },
            onAutoSkipOutro = {
                autoSkipOutro = !autoSkipOutro
            },
            onPreviousEpisode = {
                player.seekTo(0L)
            },
            onNextEpisode = onNextEpisode,
            onPortrait = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            modifier = modifier
        )
    } else {
        PortraitPlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            selectedResolution = selectedResolution,
            showResolutionMenu = showResolutionMenu,
            isPremium = isPremium,
            controlColor = controlColor,
            accent = accent,
            appBackground = appBackground,
            onLandscape = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            },
            onResolutionClick = {
                showResolutionMenu = !showResolutionMenu
            },
            onResolutionSelected = { resolution ->
                if (resolution == "1080p" && !isPremium) {
                    showResolutionMenu = false
                    showPremiumDialog = true
                } else {
                    selectedResolution = resolution
                    showResolutionMenu = false
                }
            },
            modifier = modifier
        )
    }
}

@Composable
private fun PortraitPlayer(
    player: ExoPlayer,
    position: Long,
    duration: Long,
    playing: Boolean,
    selectedResolution: String,
    showResolutionMenu: Boolean,
    isPremium: Boolean,
    controlColor: Color,
    accent: Color,
    appBackground: Color,
    onLandscape: () -> Unit,
    onResolutionClick: () -> Unit,
    onResolutionSelected: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatTime(position),
                    color = Color.White,
                    fontSize = 11.sp
                )

                Slider(
                    value = if (duration > 0) {
                        position.toFloat() / duration.toFloat()
                    } else {
                        0f
                    },
                    onValueChange = {
                        if (duration > 0) {
                            player.seekTo((it * duration).toLong())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent
                    )
                )

                Text(
                    formatTime(duration),
                    color = Color.White,
                    fontSize = 11.sp
                )

                IconButton(
                    onClick = onLandscape,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(controlColor)
                ) {
                    Icon(
                        Icons.Filled.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerCircleButton(
                    icon = Icons.Filled.Replay10,
                    contentDescription = "Mundur 10 detik",
                    size = 64.dp,
                    color = controlColor,
                    onClick = {
                        player.seekTo(
                            max(0L, player.currentPosition - 10_000L)
                        )
                    }
                )

                PlayerCircleButton(
                    icon = if (playing) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = "Play",
                    size = 82.dp,
                    color = controlColor,
                    onClick = {
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                )

                PlayerCircleButton(
                    icon = Icons.Filled.FastForward,
                    contentDescription = "Maju 10 detik",
                    size = 64.dp,
                    color = controlColor,
                    onClick = {
                        player.seekTo(
                            minOf(
                                duration,
                                player.currentPosition + 10_000L
                            )
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp)
        ) {
            Text(
                "One Piece",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Episode 1140",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(7.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    "1.2M menonton",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "24 min",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(11.dp))

            Text(
                "Mengikuti petualangan Monkey D. Luffy dan kru Topi Jerami " +
                    "dalam mencari One Piece, harta karun legendaris yang " +
                    "akan membawanya menjadi Raja Bajak Laut.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(17.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LikeDislikePanel(
                    controlColor = controlColor,
                    accent = accent
                )

                Spacer(Modifier.width(8.dp))

                Box {
                    OutlinedButton(
                        onClick = onResolutionClick,
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            selectedResolution,
                            fontWeight = FontWeight.Bold
                        )

                        if (!isPremium) {
                            Spacer(Modifier.width(5.dp))

                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Premium",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showResolutionMenu,
                        onDismissRequest = onResolutionClick
                    ) {
                        listOf("240p", "480p", "720p", "1080p")
                            .forEach { resolution ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {
                                            Text(resolution)

                                            if (
                                                resolution == "1080p" &&
                                                !isPremium
                                            ) {
                                                Spacer(Modifier.width(7.dp))

                                                Icon(
                                                    Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onResolutionSelected(resolution)
                                    }
                                )
                            }
                    }
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(controlColor)
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Download",
                        tint = accent,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Spacer(Modifier.height(21.dp))

            Text(
                "Episode List",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(
                    listOf(1138, 1139, 1140, 1141, 1142, 1143)
                ) { episode ->
                    EpisodeChip(
                        episode = episode,
                        locked = episode >= 1141,
                        selected = episode == 1140,
                        controlColor = controlColor,
                        accent = accent
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Akun",
                    tint = accent,
                    modifier = Modifier.size(43.dp)
                )

                Spacer(Modifier.width(9.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(controlColor)
                        .padding(
                            horizontal = 15.dp,
                            vertical = 12.dp
                        )
                ) {
                    Text(
                        "Tulis komentar...",
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Kirim",
                        tint = accent
                    )
                }
            }
        }
    }
}

@Composable
private fun LikeDislikePanel(
    controlColor: Color,
    accent: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(controlColor)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.ThumbUp,
            contentDescription = "Like",
            tint = accent,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            "4.5K",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(10.dp))

        Text(
            "|",
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.width(10.dp))

        Icon(
            Icons.Filled.ThumbDown,
            contentDescription = "Dislike",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            "16",
            fontWeight = FontWeight.Bold
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
    autoNext: Boolean,
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    controlColor: Color,
    accent: Color,
    appBackground: Color,
    onSpeedClick: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onAutoNext: () -> Unit,
    onAutoSkipIntro: () -> Unit,
    onAutoSkipOutro: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onPortrait: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appBackground)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPortrait) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            Column {
                Text(
                    "One Piece",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Episode 1140",
                    color = Color.White.copy(alpha = .75f),
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                PlayerPill(
                    text = "${speed}x",
                    color = controlColor,
                    onClick = onSpeedClick
                )

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = onSpeedClick
                ) {
                    listOf(.75f, 1f, 1.25f, 1.5f, 2f)
                        .forEach { value ->
                            DropdownMenuItem(
                                text = { Text("${value}x") },
                                onClick = {
                                    onSpeedSelected(value)
                                }
                            )
                        }
                }
            }

            PlayerPill(
                text = if (autoNext) "AUTO NEXT" else "AUTO NEXT OFF",
                color = controlColor,
                onClick = onAutoNext
            )

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(controlColor)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Pengaturan",
                    tint = Color.White
                )
            }
        }

        PlayerEpisodeButton(
            icon = Icons.Filled.SkipPrevious,
            text = "Ep. 1139",
            color = controlColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
            onClick = onPreviousEpisode
        )

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerCircleButton(
                icon = Icons.Filled.Replay10,
                contentDescription = "Mundur 10 detik",
                size = 62.dp,
                color = controlColor,
                onClick = {
                    player.seekTo(
                        max(0L, player.currentPosition - 10_000L)
                    )
                }
            )

            PlayerCircleButton(
                icon = if (playing) {
                    Icons.Filled.Pause
                } else {
                    Icons.Filled.PlayArrow
                },
                contentDescription = "Play",
                size = 80.dp,
                color = controlColor,
                onClick = {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            )

            PlayerCircleButton(
                icon = Icons.Filled.FastForward,
                contentDescription = "Maju 10 detik",
                size = 62.dp,
                color = controlColor,
                onClick = {
                    player.seekTo(
                        minOf(
                            duration,
                            player.currentPosition + 10_000L
                        )
                    )
                }
            )
        }

        PlayerEpisodeButton(
            icon = Icons.Filled.SkipNext,
            text = "Ep. 1141",
            color = controlColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp),
            onClick = onNextEpisode
        )

        SkipButton(
            text = "Skip Intro",
            checked = autoSkipIntro,
            color = controlColor,
            accent = accent,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 36.dp),
            onClick = onAutoSkipIntro
        )

        SkipButton(
            text = "Skip Outro",
            checked = autoSkipOutro,
            color = controlColor,
            accent = accent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 36.dp),
            onClick = onAutoSkipOutro
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatTime(position),
                color = Color.White,
                fontSize = 11.sp
            )

            Slider(
                value = if (duration > 0) {
                    position.toFloat() / duration.toFloat()
                } else {
                    0f
                },
                onValueChange = {
                    if (duration > 0) {
                        player.seekTo((it * duration).toLong())
                    }
                },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent
                )
            )

            Text(
                formatTime(duration),
                color = Color.White,
                fontSize = 11.sp
            )

            IconButton(
                onClick = onPortrait,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(controlColor)
            ) {
                Icon(
                    Icons.Filled.Fullscreen,
                    contentDescription = "Portrait",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SkipButton(
    text: String,
    checked: Boolean,
    color: Color,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.SkipNext,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(2.dp))

        Switch(
            checked = checked,
            onCheckedChange = { onClick() }
        )
    }
}

@Composable
private fun PlayerEpisodeButton(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlayerPill(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayerCircleButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    color: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(size * .38f)
        )
    }
}

@Composable
private fun EpisodeChip(
    episode: Int,
    locked: Boolean,
    selected: Boolean,
    controlColor: Color,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(width = 78.dp, height = 66.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (selected) {
                    accent.copy(alpha = .22f)
                } else {
                    controlColor
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (locked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Terkunci",
                    tint = accent,
                    modifier = Modifier.size(17.dp)
                )
            }

            Text(
                episode.toString(),
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = max(0L, ms) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "%02d:%02d".format(minutes, seconds)
}
