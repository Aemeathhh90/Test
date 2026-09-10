package com.kakaanime.app.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
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
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val colors = MaterialTheme.colorScheme

    val isLandscape =
        configuration.screenWidthDp > configuration.screenHeightDp

    val accent = colors.primary
    val playerControl = colors.surfaceVariant.copy(alpha = 0.92f)
    val playerControlStrong = colors.surfaceVariant.copy(alpha = 0.98f)

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = false
                videoScalingMode = 1
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

    var selectedQuality by remember { mutableStateOf("720p") }
    var showQualityMenu by remember { mutableStateOf(false) }

    var descriptionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying

            val seconds = position / 1000L

            val insideIntro =
                introStart > 0L &&
                introEnd > introStart &&
                seconds >= introStart &&
                seconds < introEnd

            val insideOutro =
                outroStart > 0L &&
                outroEnd > outroStart &&
                seconds >= outroStart &&
                seconds < outroEnd

            if (insideIntro && autoSkipIntro) {
                player.seekTo(introEnd * 1000L)
            }

            if (insideOutro && autoSkipOutro) {
                player.seekTo(outroEnd * 1000L)
            }

            delay(250)
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
            autoNext = autoNext,
            autoSkipIntro = autoSkipIntro,
            autoSkipOutro = autoSkipOutro,
            accent = accent,
            controlBackground = playerControl,
            controlBackgroundStrong = playerControlStrong,
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
            onBackPortrait = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            onPreviousEpisode = {},
            onNextEpisode = onNextEpisode,
            onSeekBack = {
                player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
            },
            onSeekForward = {
                player.seekTo(
                    (player.currentPosition + 10_000L)
                        .coerceAtMost(player.duration.coerceAtLeast(0L))
                )
            },
            onPlayPause = {
                if (player.isPlaying) player.pause() else player.play()
            },
            onSkipIntro = {
                if (introEnd > 0L) player.seekTo(introEnd * 1000L)
            },
            onSkipOutro = {
                if (outroEnd > 0L) player.seekTo(outroEnd * 1000L)
            },
            modifier = modifier
        )
    } else {
        PortraitPlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            selectedQuality = selectedQuality,
            showQualityMenu = showQualityMenu,
            descriptionExpanded = descriptionExpanded,
            accent = accent,
            controlBackground = playerControlStrong,
            onLandscape = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            },
            onPlayPause = {
                if (player.isPlaying) player.pause() else player.play()
            },
            onSeekBack = {
                player.seekTo(
                    (player.currentPosition - 10_000L).coerceAtLeast(0L)
                )
            },
            onSeekForward = {
                player.seekTo(
                    (player.currentPosition + 10_000L)
                        .coerceAtMost(player.duration.coerceAtLeast(0L))
                )
            },
            onQualityClick = {
                showQualityMenu = !showQualityMenu
            },
            onQualitySelected = {
                selectedQuality = it
                showQualityMenu = false
            },
            onDescriptionToggle = {
                descriptionExpanded = !descriptionExpanded
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
    selectedQuality: String,
    showQualityMenu: Boolean,
    descriptionExpanded: Boolean,
    accent: Color,
    controlBackground: Color,
    onLandscape: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onQualityClick: () -> Unit,
    onQualitySelected: (String) -> Unit,
    onDescriptionToggle: () -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp),
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    }
                }
            )

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerCircleButton(
                    icon = Icons.Filled.FastRewind,
                    description = "Mundur 10 detik",
                    size = 62.dp,
                    background = controlBackground,
                    onClick = onSeekBack
                )

                PlayerCircleButton(
                    icon = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    description = if (playing) "Pause" else "Play",
                    size = 78.dp,
                    background = controlBackground,
                    onClick = onPlayPause
                )

                PlayerCircleButton(
                    icon = Icons.Filled.FastForward,
                    description = "Maju 10 detik",
                    size = 62.dp,
                    background = controlBackground,
                    onClick = onSeekForward
                )
            }

            IconButton(
                onClick = onLandscape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(controlBackground)
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = "Landscape",
                    tint = colors.onSurface
                )
            }

            VideoProgressBar(
                position = position,
                duration = duration,
                accent = accent,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = "One Piece",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "Episode 1140  •  1.2M penonton",
                fontSize = 14.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            val description =
                "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka dalam petualangan besar di dunia One Piece. Saksikan pertarungan, misteri, dan perkembangan terbaru kru Topi Jerami."

            Text(
                text = if (descriptionExpanded)
                    description
                else
                    description.take(155) +
                        if (description.length > 155) "..." else "",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = colors.onBackground
            )

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier
                    .clickable(onClick = onDescriptionToggle)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (descriptionExpanded) "Sembunyikan" else "Selengkapnya",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionPanel(
                    accent = accent,
                    background = controlBackground,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Box {
                    CompactActionButton(
                        icon = if (selectedQuality == "1080p")
                            Icons.Filled.Lock
                        else
                            Icons.Filled.Settings,
                        text = selectedQuality,
                        accent = accent,
                        background = controlBackground,
                        onClick = onQualityClick
                    )

                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = onQualityClick
                    ) {
                        listOf("240p", "480p", "720p").forEach { quality ->
                            DropdownMenuItem(
                                text = { Text(quality) },
                                onClick = {
                                    onQualitySelected(quality)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("1080p")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Premium",
                                        modifier = Modifier.size(16.dp),
                                        tint = accent
                                    )
                                }
                            },
                            onClick = {}
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                CompactIconButton(
                    icon = Icons.Filled.Download,
                    description = "Download",
                    accent = accent,
                    background = controlBackground
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Episode List",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EpisodeCard("1138", false, false, accent)
                EpisodeCard("1139", false, false, accent)
                EpisodeCard("1140", false, true, accent)
                EpisodeCard("1141", true, false, accent)
                EpisodeCard("1142", true, false, accent)
            }

            Spacer(Modifier.height(22.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Akun",
                    tint = accent,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Komentar",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground
                    )
                    Text(
                        text = "Masuk untuk ikut berkomentar",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "Belum ada komentar. Jadilah yang pertama berkomentar.",
                    modifier = Modifier.padding(17.dp),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
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
    accent: Color,
    controlBackground: Color,
    controlBackgroundStrong: Color,
    onSpeedClick: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onAutoNext: () -> Unit,
    onAutoSkipIntro: () -> Unit,
    onAutoSkipOutro: () -> Unit,
    onBackPortrait: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipIntro: () -> Unit,
    onSkipOutro: () -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
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

        Text(
            text = "KakaAnime",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                SmallIconTextButton(
                    icon = Icons.Filled.Speed,
                    text = "${speed}x",
                    background = controlBackgroundStrong,
                    onClick = onSpeedClick
                )

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = onSpeedClick
                ) {
                    listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { value ->
                        DropdownMenuItem(
                            text = { Text("${value}x") },
                            onClick = {
                                onSpeedSelected(value)
                            }
                        )
                    }
                }
            }

            SmallTextButton(
                text = if (autoNext) "AUTO NEXT" else "AUTO NEXT OFF",
                background = controlBackgroundStrong,
                onClick = onAutoNext
            )

            IconButton(
                onClick = onBackPortrait,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(controlBackgroundStrong)
            ) {
                Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = "Portrait",
                    tint = Color.White
                )
            }
        }

        PlayerCircleButton(
            icon = Icons.Filled.SkipPrevious,
            description = "Episode 1139",
            size = 56.dp,
            background = controlBackground,
            onClick = onPreviousEpisode,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
        )

        PlayerCircleButton(
            icon = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            description = if (playing) "Pause" else "Play",
            size = 72.dp,
            background = controlBackground,
            onClick = onPlayPause,
            modifier = Modifier.align(Alignment.Center)
        )

        PlayerCircleButton(
            icon = Icons.Filled.SkipNext,
            description = "Episode 1141",
            size = 56.dp,
            background = controlBackground,
            onClick = onNextEpisode,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallIconTextButton(
                icon = Icons.Filled.FastRewind,
                text = "Intro",
                background = controlBackground,
                onClick = onSkipIntro
            )

            SmallIconTextButton(
                icon = Icons.Filled.FastRewind,
                text = "Auto Intro",
                background = controlBackground,
                onClick = onAutoSkipIntro
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 34.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallIconTextButton(
                icon = Icons.Filled.FastForward,
                text = "Auto Outro",
                background = controlBackground,
                onClick = onAutoSkipOutro
            )

            SmallIconTextButton(
                icon = Icons.Filled.FastForward,
                text = "Outro",
                background = controlBackground,
                onClick = onSkipOutro
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(position),
                fontSize = 11.sp,
                color = Color.White
            )

            Spacer(Modifier.width(6.dp))

            VideoProgressBar(
                position = position,
                duration = duration,
                accent = accent,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = formatTime(duration),
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ReactionPanel(
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(background)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ThumbUp,
            contentDescription = "Like",
            tint = accent,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(7.dp))

        Text(
            text = "4.5K",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(23.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Filled.ThumbDown,
            contentDescription = "Dislike",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(7.dp))

        Text(
            text = "16",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EpisodeCard(
    number: String,
    locked: Boolean,
    selected: Boolean,
    accent: Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .width(104.dp)
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected)
            accent
        else
            colors.surfaceVariant.copy(alpha = if (locked) 0.45f else 0.75f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = number,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected)
                        colors.onPrimary
                    else
                        colors.onSurface
                )

                if (locked) {
                    Spacer(Modifier.height(3.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Terkunci",
                        tint = accent,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accent: Color,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(6.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CompactIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    accent: Color,
    background: Color
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable {}
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = accent,
            modifier = Modifier
                .size(21.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun SmallIconTextButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SmallTextButton(
    text: String,
    background: Color,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
private fun PlayerCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    size: androidx.compose.ui.unit.Dp,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}

@Composable
private fun VideoProgressBar(
    position: Long,
    duration: Long,
    accent: Color,
    modifier: Modifier
) {
    val progress =
        if (duration > 0L)
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        else
            0f

    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxSize()
                .background(accent)
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
