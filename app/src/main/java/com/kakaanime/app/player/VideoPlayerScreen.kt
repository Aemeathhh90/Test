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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class EpisodeUi(
    val number: Int,
    val thumbnail: String,
    val locked: Boolean = false
)

private val demoEpisodes = listOf(
    EpisodeUi(
        1138,
        "https://picsum.photos/seed/kakaanime1138/640/360"
    ),
    EpisodeUi(
        1139,
        "https://picsum.photos/seed/kakaanime1139/640/360"
    ),
    EpisodeUi(
        1140,
        "https://picsum.photos/seed/kakaanime1140/640/360"
    ),
    EpisodeUi(
        1141,
        "https://picsum.photos/seed/kakaanime1141/640/360",
        locked = true
    )
)

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

    val isLandscape =
        configuration.screenWidthDp > configuration.screenHeightDp

    val colors = MaterialTheme.colorScheme

    val accent = colors.primary
    val controlBackground = colors.surfaceVariant.copy(alpha = 0.72f)
    val controlBackgroundStrong = colors.surfaceVariant.copy(alpha = 0.86f)

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
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

    var selectedEpisode by remember { mutableStateOf(1140) }
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
                introStart > 0 &&
                    introEnd > introStart &&
                    seconds >= introStart &&
                    seconds < introEnd

            val insideOutro =
                outroStart > 0 &&
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
            controlBackground = controlBackground,
            controlBackgroundStrong = controlBackgroundStrong,
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
            onPreviousEpisode = {
                if (selectedEpisode > 1) {
                    selectedEpisode--
                }
            },
            onNextEpisode = {
                selectedEpisode++
                onNextEpisode()
            },
            onSeekBack = {
                player.seekTo(
                    (player.currentPosition - 10_000L)
                        .coerceAtLeast(0L)
                )
            },
            onSeekForward = {
                player.seekTo(
                    (player.currentPosition + 10_000L)
                        .coerceAtMost(
                            player.duration.coerceAtLeast(0L)
                        )
                )
            },
            modifier = modifier
        )
    } else {
        PortraitPlayer(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            selectedEpisode = selectedEpisode,
            selectedQuality = selectedQuality,
            showQualityMenu = showQualityMenu,
            descriptionExpanded = descriptionExpanded,
            accent = accent,
            controlBackground = controlBackground,
            controlBackgroundStrong = controlBackgroundStrong,
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
            onEpisodeSelected = {
                if (!it.locked) {
                    selectedEpisode = it.number
                }
            },
            onLandscape = {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            },
            onSeekBack = {
                player.seekTo(
                    (player.currentPosition - 10_000L)
                        .coerceAtLeast(0L)
                )
            },
            onSeekForward = {
                player.seekTo(
                    (player.currentPosition + 10_000L)
                        .coerceAtMost(
                            player.duration.coerceAtLeast(0L)
                        )
                )
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
    selectedEpisode: Int,
    selectedQuality: String,
    showQualityMenu: Boolean,
    descriptionExpanded: Boolean,
    accent: Color,
    controlBackground: Color,
    controlBackgroundStrong: Color,
    onQualityClick: () -> Unit,
    onQualitySelected: (String) -> Unit,
    onDescriptionToggle: () -> Unit,
    onEpisodeSelected: (EpisodeUi) -> Unit,
    onLandscape: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        PortraitVideo(
            player = player,
            position = position,
            duration = duration,
            playing = playing,
            accent = accent,
            controlBackground = controlBackground,
            onLandscape = onLandscape,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 24.dp
                )
        ) {
            Text(
                text = "One Piece",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Episode $selectedEpisode",
                fontSize = 16.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "1.2M menonton",
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )

                Text(
                    text = "  •  ",
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )

                Text(
                    text = "24 min",
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            val description =
                "Mengikuti petualangan Monkey D. Luffy dan kru Topi Jerami " +
                    "dalam mencari One Piece, harta karun legendaris yang " +
                    "akan membawanya menjadi Raja Bajak Laut."

            Text(
                text = if (descriptionExpanded) {
                    description
                } else {
                    description.take(155) +
                        if (description.length > 155) "..." else ""
                },
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = colors.onBackground
            )

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier.clickable {
                    onDescriptionToggle()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selengkapnya",
                    fontSize = 14.sp,
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
                    background = controlBackground
                )

                Spacer(Modifier.width(10.dp))

                Box {
                    QualityButton(
                        quality = selectedQuality,
                        accent = accent,
                        background = controlBackground,
                        onClick = onQualityClick
                    )

                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = onQualityClick
                    ) {
                        DropdownMenuItem(
                            text = { Text("240p") },
                            onClick = {
                                onQualitySelected("240p")
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("480p") },
                            onClick = {
                                onQualitySelected("480p")
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("720p") },
                            onClick = {
                                onQualitySelected("720p")
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text("1080p")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector =
                                            Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            onClick = {
                                // Premium-only placeholder.
                            }
                        )
                    }
                }

                Spacer(
                    Modifier.weight(1f)
                )

                SmallIconButton(
                    icon = Icons.Filled.Download,
                    contentDescription = "Download",
                    accent = accent,
                    background = controlBackgroundStrong
                )
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Episode List",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Lihat Semua",
                    fontSize = 13.sp,
                    color = accent
                )

                Icon(
                    imageVector = Icons.Filled.NavigateNext,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                demoEpisodes.forEach { episode ->
                    EpisodeThumbnail(
                        episode = episode,
                        selected =
                            episode.number == selectedEpisode,
                        accent = accent,
                        onClick = {
                            onEpisodeSelected(episode)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Komentar",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "(320)",
                    fontSize = 15.sp,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                Surface(
                    color = controlBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Terbaru",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = 0.18f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Akun",
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = controlBackground
                ) {
                    Text(
                        text = "Tulis komentar...",
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        fontSize = 14.sp,
                        color = colors.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Kirim",
                    tint = accent,
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    }
}

@Composable
private fun PortraitVideo(
    player: ExoPlayer,
    position: Long,
    duration: Long,
    playing: Boolean,
    accent: Color,
    controlBackground: Color,
    onLandscape: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                    setShutterBackgroundColor(
                        android.graphics.Color.BLACK
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            MiniPill(
                text = "1.0x",
                background = controlBackground
            )

            Spacer(Modifier.width(6.dp))

            MiniPill(
                text = "AUTO NEXT",
                background = controlBackground
            )

            Spacer(Modifier.width(6.dp))

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        controlBackground,
                        RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Pengaturan",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            PlayerCircleButton(
                icon = Icons.Filled.Replay10,
                size = 58.dp,
                background = controlBackground,
                onClick = onSeekBack
            )

            PlayerCircleButton(
                icon =
                    if (playing) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                size = 72.dp,
                background = controlBackground,
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
                size = 58.dp,
                background = controlBackground,
                onClick = onSeekForward
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 5.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(position),
                fontSize = 11.sp,
                color = Color.White
            )

            Spacer(Modifier.width(5.dp))

            VideoProgressBar(
                position = position,
                duration = duration,
                accent = accent,
                onSeek = {
                    player.seekTo(it)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = formatTime(duration),
                fontSize = 11.sp,
                color = Color.White
            )

            Spacer(Modifier.width(5.dp))

            IconButton(
                onClick = onLandscape,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        controlBackground,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
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
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                    setShutterBackgroundColor(
                        android.graphics.Color.BLACK
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPortrait,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            Column {
                Text(
                    text = "One Piece",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Episode 1140",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }

            Spacer(Modifier.weight(1f))

            Box {
                SmallIconButtonWithText(
                    icon = Icons.Filled.Speed,
                    text = "${speed}x",
                    background = controlBackground,
                    onClick = onSpeedClick
                )

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = onSpeedClick
                ) {
                    listOf(
                        0.75f,
                        1f,
                        1.25f,
                        1.5f,
                        2f
                    ).forEach { value ->
                        DropdownMenuItem(
                            text = {
                                Text("${value}x")
                            },
                            onClick = {
                                onSpeedSelected(value)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.width(6.dp))

            MiniPill(
                text = "AUTO NEXT",
                background = controlBackground
            )

            Spacer(Modifier.width(6.dp))

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        controlBackground,
                        RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Pengaturan",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        EpisodeNavButton(
            text = "Ep. 1139",
            icon = Icons.Filled.SkipPrevious,
            background = controlBackground,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
            onClick = onPreviousEpisode
        )

        EpisodeNavButton(
            text = "Ep. 1141",
            icon = Icons.Filled.SkipNext,
            background = controlBackground,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp),
            onClick = onNextEpisode
        )

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            PlayerCircleButton(
                icon = Icons.Filled.Replay10,
                size = 54.dp,
                background = controlBackground,
                onClick = onSeekBack
            )

            PlayerCircleButton(
                icon =
                    if (playing) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
                size = 68.dp,
                background = controlBackgroundStrong,
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
                size = 54.dp,
                background = controlBackground,
                onClick = onSeekForward
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 18.dp,
                    bottom = 32.dp
                ),
            color = controlBackground,
            shape = RoundedCornerShape(13.dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 4.dp,
                    top = 5.dp,
                    bottom = 5.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "Skip Intro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(Modifier.width(6.dp))

                Switch(
                    checked = autoSkipIntro,
                    onCheckedChange = {
                        onAutoSkipIntro()
                    },
                    modifier = Modifier
                        .height(28.dp)
                        .width(48.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = 32.dp
                ),
            color = controlBackground,
            shape = RoundedCornerShape(13.dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 4.dp,
                    top = 5.dp,
                    bottom = 5.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "Skip Outro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(Modifier.width(6.dp))

                Switch(
                    checked = autoSkipOutro,
                    onCheckedChange = {
                        onAutoSkipOutro()
                    },
                    modifier = Modifier
                        .height(28.dp)
                        .width(48.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    bottom = 7.dp
                ),
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
                onSeek = {
                    player.seekTo(it)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = formatTime(duration),
                fontSize = 11.sp,
                color = Color.White
            )

            Spacer(Modifier.width(5.dp))

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        controlBackground,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = "Keluar fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoProgressBar(
    position: Long,
    duration: Long,
    accent: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier
) {
    val max = duration.coerceAtLeast(1L).toFloat()

    Slider(
        value = position.coerceIn(
            0L,
            duration.coerceAtLeast(1L)
        ).toFloat(),
        onValueChange = {
            onSeek(it.roundToInt().toLong())
        },
        valueRange = 0f..max,
        modifier = modifier.height(28.dp),
        colors = SliderDefaults.colors(
            thumbColor = accent,
            activeTrackColor = accent,
            inactiveTrackColor =
                Color.White.copy(alpha = 0.28f)
        )
    )
}

@Composable
private fun PlayerCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        color = background,
        shape = RoundedCornerShape(50)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(
                    if (size.value >= 68f) 32.dp
                    else 25.dp
                )
            )
        }
    }
}

@Composable
private fun ReactionPanel(
    accent: Color,
    background: Color
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = "Like",
                tint = accent,
                modifier = Modifier.size(23.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = "4.5K",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = "|",
                color = Color.White.copy(alpha = 0.45f)
            )

            Spacer(Modifier.width(14.dp))

            Icon(
                imageVector = Icons.Filled.ThumbDown,
                contentDescription = "Dislike",
                tint = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.size(23.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = "16",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QualityButton(
    quality: String,
    accent: Color,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = background,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 17.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quality,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            Spacer(Modifier.width(7.dp))

            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Premium",
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    accent: Color,
    background: Color
) {
    Surface(
        modifier = Modifier.size(48.dp),
        color = background,
        shape = RoundedCornerShape(15.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SmallIconButtonWithText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        color = background,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 9.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MiniPill(
    text: String,
    background: Color
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 9.dp
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun EpisodeNavButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EpisodeThumbnail(
    episode: EpisodeUi,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(115.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = episode.thumbnail,
            contentDescription = "Episode ${episode.number}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = if (episode.locked) {
                            0.52f
                        } else {
                            0.18f
                        }
                    )
                )
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        accent.copy(alpha = 0.15f)
                    )
            )
        }

        Text(
            text = episode.number.toString(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 8.dp,
                    bottom = 6.dp
                ),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (episode.locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Terkunci",
                tint = accent,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(23.dp)
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds =
        (milliseconds / 1000L).coerceAtLeast(0L)

    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "%02d:%02d".format(
        minutes,
        seconds
    )
}
