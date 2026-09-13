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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kakaanime.app.player.core.rememberPlayerController
import com.kakaanime.app.player.core.rememberPlayerState
import com.kakaanime.app.player.ui.ReDantotsuPlayerController
import com.kakaanime.app.provider.ProviderPlaybackResolver
import com.kakaanime.app.provider.StreamQuality
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    title: String = "One Piece",
    episodeNumber: Int = 1140,
    description: String = "",
    introStart: Long = 0L,
    introEnd: Long = 0L,
    outroStart: Long = 0L,
    outroEnd: Long = 0L,
    isPremium: Boolean = false,
    modifier: Modifier = Modifier,
    onPreviousEpisode: () -> Unit = {},
    onNextEpisode: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val colors = MaterialTheme.colorScheme
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val playerController = rememberPlayerController(context)
    val playerState = rememberPlayerState(playerController)
    val player = playerController.player

    var autoNext by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("720p") }
    var showQualityMenu by remember { mutableStateOf(false) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var qualityRequest by remember { mutableStateOf<String?>(null) }
    var qualityLoading by remember { mutableStateOf(false) }

    LaunchedEffect(videoUrl) { playerController.setVideo(videoUrl) }

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
            val resolved = runCatching {
                ProviderPlaybackResolver.resolve(
                    title = title,
                    episodeNumber = episodeNumber,
                    premium = isPremium,
                    preferredQuality = target
                )
            }.getOrNull()
            if (resolved != null && resolved.url.isNotBlank()) {
                playerController.setVideo(resolved.url)
                playerController.seekTo(position)
                selectedQuality = requested
            }
        }
        qualityRequest = null
        qualityLoading = false
    }

    LaunchedEffect(isPremium, introStart, introEnd, outroStart, outroEnd, videoUrl) {
        if (!isPremium) return@LaunchedEffect
        while (true) {
            delay(500L)
            val position = player.currentPosition.coerceAtLeast(0L)
            if (introEnd > introStart && position in introStart until introEnd) playerController.seekTo(introEnd)
            else if (outroEnd > outroStart && position in outroStart until outroEnd) playerController.seekTo(outroEnd)
        }
    }

    val previousEpisode = (episodeNumber - 1).coerceAtLeast(1)
    val nextEpisode = episodeNumber + 1

    if (isLandscape) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
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
        Column(modifier = modifier.fillMaxSize().background(colors.background).navigationBarsPadding()) {
            Box(modifier = Modifier.fillMaxWidth().height(245.dp).background(Color.Black)) {
                PlayerSurface(player)
                ReDantotsuPlayerController(
                    state = playerState,
                    title = "$title • Episode $episodeNumber",
                    previousEpisode = previousEpisode.toString(),
                    nextEpisode = nextEpisode.toString(),
                    autoNext = autoNext,
                    onAutoNext = { autoNext = !autoNext },
                    onBack = {},
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

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                Spacer(Modifier.height(3.dp))
                Text("Episode $episodeNumber", fontSize = 14.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                if (description.isNotBlank()) {
                    Text(if (descriptionExpanded) description else description.take(155) + if (description.length > 155) "..." else "", fontSize = 14.sp, lineHeight = 21.sp, color = colors.onBackground)
                    Row(modifier = Modifier.clickable { descriptionExpanded = !descriptionExpanded }.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (descriptionExpanded) "Sembunyikan" else "Selengkapnya", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
                        Icon(Icons.Filled.ExpandMore, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ReactionPanel(modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Box {
                        QualityButton(if (qualityLoading) "..." else selectedQuality) { if (!qualityLoading) showQualityMenu = true }
                        DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                            listOf("360p", "480p", "720p").forEach { quality ->
                                DropdownMenuItem(text = { Text(if (quality == selectedQuality) "✓ $quality" else quality) }, onClick = { qualityRequest = quality; showQualityMenu = false })
                            }
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) { Text("1080p"); Spacer(Modifier.width(8.dp)); Icon(Icons.Filled.Lock, "Premium", tint = colors.primary, modifier = Modifier.size(16.dp)) } },
                                onClick = { if (isPremium) { qualityRequest = "1080p"; showQualityMenu = false } }
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Download, "Download", tint = colors.primary, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant.copy(alpha = 0.75f)).clickable {}.padding(14.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text("Episode List", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EpisodeCard(previousEpisode.toString(), false, false)
                    EpisodeCard(episodeNumber.toString(), false, true)
                    EpisodeCard(nextEpisode.toString(), true, false)
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountCircle, "Akun", tint = colors.primary, modifier = Modifier.size(42.dp))
                    Spacer(Modifier.width(10.dp))
                    Column { Text("Komentar", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = colors.onBackground); Text("Masuk untuk ikut berkomentar", fontSize = 12.sp, color = colors.onSurfaceVariant) }
                }
                Spacer(Modifier.height(10.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.surfaceVariant.copy(alpha = 0.35f)) {
                    Text("Belum ada komentar. Jadilah yang pertama berkomentar.", modifier = Modifier.padding(17.dp), fontSize = 14.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PlayerSurface(player: Player) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context -> PlayerView(context).apply { this.player = player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) } }, update = { it.player = player })
}

@Composable
private fun SpeedMenu(speed: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        DropdownMenu(expanded = true, onDismissRequest = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
            listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { value -> DropdownMenuItem(text = { Text(if (value == speed) "✓ ${value}x" else "${value}x") }, onClick = { onSelect(value) }) }
        }
    }
}

@Composable
private fun ReactionPanel(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier.height(50.dp).clip(RoundedCornerShape(25.dp)).background(colors.surfaceVariant.copy(alpha = 0.92f)).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.ThumbUp, "Like", tint = colors.primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(7.dp)); Text("4.5K", fontWeight = FontWeight.Bold, color = colors.onSurface); Spacer(Modifier.width(12.dp)); Box(Modifier.width(1.dp).height(23.dp).background(colors.onSurface.copy(alpha = 0.25f))); Spacer(Modifier.width(12.dp)); Icon(Icons.Filled.ThumbDown, "Dislike", tint = colors.onSurface, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(7.dp)); Text("16", fontWeight = FontWeight.Bold, color = colors.onSurface)
    }
}

@Composable
private fun QualityButton(quality: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.height(50.dp).clip(RoundedCornerShape(16.dp)).background(colors.surfaceVariant.copy(alpha = 0.92f)).clickable(onClick = onClick).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(quality, fontWeight = FontWeight.Bold, color = colors.onSurface); Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.Settings, "Kualitas", tint = colors.primary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EpisodeCard(number: String, locked: Boolean, selected: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = Modifier.width(104.dp).height(72.dp), shape = RoundedCornerShape(16.dp), color = if (selected) colors.primary else colors.surfaceVariant.copy(alpha = if (locked) 0.45f else 0.75f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(number, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (selected) colors.onPrimary else colors.onSurface)
            if (locked) { Spacer(Modifier.height(3.dp)); Icon(Icons.Filled.Lock, "Terkunci", tint = colors.primary, modifier = Modifier.size(17.dp)) }
        }
    }
}
