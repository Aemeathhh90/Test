package com.kakaanime.app.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kakaanime.app.player.core.rememberPlayerController
import com.kakaanime.app.player.core.rememberPlayerState
import com.kakaanime.app.player.ui.ReDantotsuPlayerController
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderPlaybackResolver
import com.kakaanime.app.provider.StreamQuality
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(videoUrl: String, title: String = "One Piece", episodeNumber: Int = 1140, description: String = "", introStart: Long = 0L, introEnd: Long = 0L, outroStart: Long = 0L, outroEnd: Long = 0L, isPremium: Boolean = false, episodes: List<ProviderEpisode> = emptyList(), watchedEpisodes: Set<Int> = emptySet(), modifier: Modifier = Modifier, onBack: () -> Unit = {}, onPreviousEpisode: () -> Unit = {}, onNextEpisode: () -> Unit = {}, onEpisodeClick: (Int) -> Unit = {}, onRenderedFirstFrame: () -> Unit = {}) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val colors = MaterialTheme.colorScheme
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val settings = remember(context) { context.getSharedPreferences("kakaanime_settings", Context.MODE_PRIVATE) }
    var autoNext by remember { mutableStateOf(settings.getBoolean("auto_next", true)) }
    var autoSkipIntro by remember { mutableStateOf(settings.getBoolean("auto_skip_intro", false)) }
    var autoSkipOutro by remember { mutableStateOf(settings.getBoolean("auto_skip_outro", false)) }
    var speed by remember { mutableFloatStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf(settings.getString("default_quality", "Auto") ?: "Auto") }
    var showQualityMenu by remember { mutableStateOf(false) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var qualityRequest by remember { mutableStateOf<String?>(null) }
    var qualityLoading by remember { mutableStateOf(false) }

    val playerController = rememberPlayerController(context)
    val playerState = rememberPlayerState(playerController)
    val player = playerController.player

    DisposableEffect(player, videoUrl, episodeNumber) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                onRenderedFirstFrame()
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(videoUrl) { playerController.setVideo(videoUrl) }
    LaunchedEffect(Unit) {
        autoNext = settings.getBoolean("auto_next", true)
        autoSkipIntro = settings.getBoolean("auto_skip_intro", false)
        autoSkipOutro = settings.getBoolean("auto_skip_outro", false)
        selectedQuality = settings.getString("default_quality", "Auto") ?: "Auto"
    }
    LaunchedEffect(autoNext, autoSkipIntro, autoSkipOutro, selectedQuality) {
        settings.edit().putBoolean("auto_next", autoNext).putBoolean("auto_skip_intro", autoSkipIntro).putBoolean("auto_skip_outro", autoSkipOutro).putString("default_quality", selectedQuality).apply()
    }
    LaunchedEffect(videoUrl, title, episodeNumber, isPremium, selectedQuality) {
        if (selectedQuality == "Auto") return@LaunchedEffect
        val target = when (selectedQuality) {
            "360p" -> StreamQuality.Q360
            "480p" -> StreamQuality.Q480
            "720p" -> StreamQuality.Q720
            "1080p" -> StreamQuality.Q1080
            else -> null
        } ?: return@LaunchedEffect
        if (selectedQuality == "1080p" && !isPremium) return@LaunchedEffect
        val position = player.currentPosition.coerceAtLeast(0L)
        val resolved = runCatching { ProviderPlaybackResolver.resolve(title = title, episodeNumber = episodeNumber, premium = isPremium, preferredQuality = target) }.getOrNull()
        if (resolved != null && resolved.url.isNotBlank() && resolved.url != videoUrl) {
            playerController.setVideo(resolved.url)
            if (position > 0L) playerController.seekTo(position)
        }
    }
    LaunchedEffect(qualityRequest, title, episodeNumber, isPremium) {
        val requested = qualityRequest ?: return@LaunchedEffect
        qualityLoading = true
        val target = when (requested) {
            "360p" -> StreamQuality.Q360
            "480p" -> StreamQuality.Q480
            "720p" -> StreamQuality.Q720
            "1080p" -> StreamQuality.Q1080
            else -> null
        }
        if (target != null && (requested != "1080p" || isPremium)) {
            val position = player.currentPosition.coerceAtLeast(0L)
            val resolved = runCatching { ProviderPlaybackResolver.resolve(title = title, episodeNumber = episodeNumber, premium = isPremium, preferredQuality = target) }.getOrNull()
            if (resolved != null && resolved.url.isNotBlank()) {
                playerController.setVideo(resolved.url)
                if (position > 0L) playerController.seekTo(position)
                selectedQuality = requested
            }
        }
        qualityRequest = null
        qualityLoading = false
    }
    LaunchedEffect(isPremium, autoSkipIntro, autoSkipOutro, introStart, introEnd, outroStart, outroEnd, videoUrl) {
        if (!isPremium || (!autoSkipIntro && !autoSkipOutro)) return@LaunchedEffect
        while (true) {
            delay(500L)
            val position = player.currentPosition.coerceAtLeast(0L)
            if (autoSkipIntro && introEnd > introStart && position in introStart until introEnd) playerController.seekTo(introEnd)
            else if (autoSkipOutro && outroEnd > outroStart && position in outroStart until outroEnd) playerController.seekTo(outroEnd)
        }
    }

    val episodeNumbers = episodes.map { it.number }.filter { it > 0 }.distinct().sorted()
    val previousEpisode = episodeNumbers.lastOrNull { it < episodeNumber } ?: ((episodeNumber - 1).coerceAtLeast(1))
    val nextEpisode = episodeNumbers.firstOrNull { it > episodeNumber } ?: (episodeNumber + 1)
    val maxEpisode = episodeNumbers.maxOrNull() ?: episodeNumber

    if (isLandscape) {
        Box(modifier.fillMaxSize().background(Color.Black)) {
            PlayerSurface(player)
            ReDantotsuPlayerController(
                state = playerState,
                title = "$title • Episode $episodeNumber",
                previousEpisode = previousEpisode.toString(),
                nextEpisode = nextEpisode.toString(),
                autoNext = autoNext,
                onAutoNext = { autoNext = !autoNext },
                onBack = { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
                onPlayPause = { playerController.togglePlayPause() },
                onSeekBack = { playerController.seekBack() },
                onSeekForward = { playerController.seekForward() },
                onSeekTo = { playerController.seekTo(it) },
                onPreviousEpisode = onPreviousEpisode,
                onNextEpisode = onNextEpisode,
                onFullscreen = { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
                onSpeed = { showSpeedMenu = true }
            )
            if (showSpeedMenu) SpeedMenu(speed, { speed = it; playerController.setSpeed(it); showSpeedMenu = false }, { showSpeedMenu = false })
        }
    } else {
        Column(modifier.fillMaxSize().background(colors.background).navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(245.dp).background(Color.Black)) {
                PlayerSurface(player)
                ReDantotsuPlayerController(
                    state = playerState,
                    title = "$title • Episode $episodeNumber",
                    previousEpisode = previousEpisode.toString(),
                    nextEpisode = nextEpisode.toString(),
                    autoNext = autoNext,
                    onAutoNext = { autoNext = !autoNext },
                    onBack = onBack,
                    onPlayPause = { playerController.togglePlayPause() },
                    onSeekBack = { playerController.seekBack() },
                    onSeekForward = { playerController.seekForward() },
                    onSeekTo = { playerController.seekTo(it) },
                    onPreviousEpisode = onPreviousEpisode,
                    onNextEpisode = onNextEpisode,
                    onFullscreen = { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE },
                    onSpeed = { showSpeedMenu = true }
                )
            }
            if (showSpeedMenu) SpeedMenu(speed, { speed = it; playerController.setSpeed(it); showSpeedMenu = false }, { showSpeedMenu = false })
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                Spacer(Modifier.height(3.dp))
                Text("Episode $episodeNumber", fontSize = 14.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                if (description.isNotBlank()) {
                    Text(if (descriptionExpanded) description else description.take(155) + if (description.length > 155) "..." else "", fontSize = 14.sp, lineHeight = 21.sp, color = colors.onBackground)
                    Row(Modifier.clickable { descriptionExpanded = !descriptionExpanded }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (descriptionExpanded) "Sembunyikan" else "Selengkapnya", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
                        Icon(Icons.Filled.ExpandMore, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Kualitas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Box {
                        QualityButton(if (qualityLoading) "..." else selectedQuality) { if (!qualityLoading) showQualityMenu = true }
                        DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                            listOf("360p", "480p", "720p").forEach { quality ->
                                DropdownMenuItem(text = { Text(if (quality == selectedQuality) "✓ $quality" else quality) }, onClick = { qualityRequest = quality; showQualityMenu = false })
                            }
                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Text("1080p"); Spacer(Modifier.width(8.dp)); Icon(Icons.Filled.Lock, "Premium", tint = colors.primary, modifier = Modifier.size(16.dp)) } }, onClick = { if (isPremium) { qualityRequest = "1080p"; showQualityMenu = false } })
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Episode List", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                    Spacer(Modifier.width(8.dp))
                    if (episodeNumbers.isNotEmpty()) Text("$episodeNumber / $maxEpisode", fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                if (episodeNumbers.isEmpty()) {
                    Text("Episode provider belum tersedia.", fontSize = 14.sp, color = colors.onSurfaceVariant)
                } else {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        episodeNumbers.forEach { number -> EpisodeCard(number.toString(), number !in watchedEpisodes && number != episodeNumber, number == episodeNumber) { onEpisodeClick(number) } }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.surfaceVariant.copy(alpha = .35f)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ChatBubbleOutline, null, tint = colors.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Komentar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Fitur komentar akan hadir di V2.", fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun PlayerSurface(player: Player) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { it.player = player }
    )
}

@Composable private fun SpeedMenu(speed: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        DropdownMenu(expanded = true, onDismissRequest = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
            listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { value ->
                DropdownMenuItem(text = { Text(if (value == speed) "✓ ${value}x" else "${value}x") }, onClick = { onSelect(value) })
            }
        }
    }
}

@Composable private fun QualityButton(quality: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.height(50.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant.copy(alpha = .92f)).clickable(onClick = onClick).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(quality, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.Settings, "Kualitas", tint = colors.primary, modifier = Modifier.size(18.dp))
    }
}

@Composable private fun EpisodeCard(number: String, locked: Boolean, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val showProgress = !locked
    Surface(Modifier.width(92.dp).height(62.dp).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), color = if (selected) colors.primary else colors.surfaceVariant.copy(alpha = if (locked) .45f else .78f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(number, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (selected) colors.onPrimary else colors.onSurface)
                if (locked) { Spacer(Modifier.width(5.dp)); Icon(Icons.Filled.Lock, "Terkunci", tint = colors.primary, modifier = Modifier.size(15.dp)) }
            }
            if (showProgress) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(progress = { 1f }, modifier = Modifier.width(68.dp).height(3.dp).clip(RoundedCornerShape(3.dp)), color = if (selected) colors.onPrimary else colors.primary, trackColor = colors.onSurface.copy(alpha = .14f))
            }
        }
    }
}
