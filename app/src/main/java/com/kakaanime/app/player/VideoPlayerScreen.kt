package com.kakaanime.app.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kakaanime.app.player.core.rememberPlayerController
import com.kakaanime.app.player.core.rememberPlayerState
import com.kakaanime.app.player.ui.ReDantotsuPlayerController
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderPlaybackResolver
import com.kakaanime.app.provider.StreamQuality
import kotlinx.coroutines.delay
import java.util.Locale

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
    episodes: List<ProviderEpisode> = emptyList(),
    watchedEpisodes: Set<Int> = emptySet(),
    seasonNumber: Int? = null,
    seasonTitle: String? = null,
    unlockRemainingSeconds: Int? = null,
    onCancelUnlock: () -> Unit = {},
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onPreviousEpisode: () -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onEpisodeClick: (Int) -> Unit = {},
    onRenderedFirstFrame: () -> Unit = {},
) {
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
    val currentOnRenderedFirstFrame by rememberUpdatedState(onRenderedFirstFrame)

    DisposableEffect(player, videoUrl, episodeNumber) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() { currentOnRenderedFirstFrame() }
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
        settings.edit()
            .putBoolean("auto_next", autoNext)
            .putBoolean("auto_skip_intro", autoSkipIntro)
            .putBoolean("auto_skip_outro", autoSkipOutro)
            .putString("default_quality", selectedQuality)
            .apply()
    }

    LaunchedEffect(qualityRequest, title, episodeNumber, seasonNumber, seasonTitle, isPremium, videoUrl) {
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
                    preferredQuality = target,
                    seasonNumber = seasonNumber,
                    seasonTitle = seasonTitle,
                )
            }.getOrNull()
            if (resolved != null && resolved.url.isNotBlank() && resolved.url != player.currentMediaItem?.localConfiguration?.uri?.toString()) {
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
    val previousEpisode = episodeNumbers.lastOrNull { it < episodeNumber } ?: (episodeNumber - 1).coerceAtLeast(1)
    val nextEpisode = episodeNumbers.firstOrNull { it > episodeNumber } ?: (episodeNumber + 1)
    val maxEpisode = episodeNumbers.maxOrNull() ?: episodeNumber

    if (isLandscape) {
        Box(modifier.fillMaxSize().background(Color.Black)) {
            PlayerSurface(player)
            LandscapePlayerTopBar(
                title = title,
                episodeNumber = episodeNumber,
                onBack = { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
            )
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
                onSpeed = { showSpeedMenu = true },
            )
            LandscapeEpisodeRail(
                modifier = Modifier.align(Alignment.BottomCenter),
                previousEpisode = previousEpisode,
                currentEpisode = episodeNumber,
                nextEpisode = nextEpisode,
                episodes = episodes,
                watchedEpisodes = watchedEpisodes,
                onPreviousEpisode = onPreviousEpisode,
                onNextEpisode = onNextEpisode,
                onEpisodeClick = onEpisodeClick,
            )
            if (unlockRemainingSeconds != null) PlayerUnlockOverlay(unlockRemainingSeconds, onCancelUnlock)
            if (showSpeedMenu) SpeedMenu(speed, { value -> speed = value; playerController.setSpeed(value); showSpeedMenu = false }, { showSpeedMenu = false })
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().background(colors.background),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
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
                        onSpeed = { showSpeedMenu = true },
                    )
                    if (unlockRemainingSeconds != null) PlayerUnlockOverlay(unlockRemainingSeconds, onCancelUnlock)
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text("Episode $episodeNumber", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                        if (unlockRemainingSeconds == null) TextButton(onClick = onNextEpisode) { Text("Berikutnya") }
                    }
                    Spacer(Modifier.height(14.dp))
                    PlayerQuickActions(
                        quality = if (qualityLoading) "..." else selectedQuality,
                        speed = speed,
                        onQuality = { if (!qualityLoading) showQualityMenu = true },
                        onSpeed = { showSpeedMenu = true },
                        onFullscreen = { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE },
                    )
                    DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                        listOf("360p", "480p", "720p").forEach { quality ->
                            DropdownMenuItem(text = { Text(if (quality == selectedQuality) "✓ $quality" else quality) }, onClick = { qualityRequest = quality; showQualityMenu = false })
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("1080p")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Lock, "Premium", tint = colors.primary, modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = { if (isPremium) { qualityRequest = "1080p"; showQualityMenu = false } },
                        )
                    }
                    if (showSpeedMenu) SpeedMenu(speed, { value -> speed = value; playerController.setSpeed(value); showSpeedMenu = false }, { showSpeedMenu = false })
                }
            }
            if (description.isNotBlank()) {
                item {
                    Surface(Modifier.padding(horizontal = 18.dp), shape = RoundedCornerShape(18.dp), color = colors.surfaceVariant.copy(alpha = .38f)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Tentang Episode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(if (descriptionExpanded || description.length <= 180) description else description.take(180) + "...", fontSize = 14.sp, lineHeight = 21.sp, color = colors.onSurfaceVariant)
                            if (description.length > 180) TextButton(onClick = { descriptionExpanded = !descriptionExpanded }) { Text(if (descriptionExpanded) "Sembunyikan" else "Selengkapnya") }
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoLibrary, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Daftar Episode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("$episodeNumber / $maxEpisode", fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
            }
            item {
                if (episodeNumbers.isEmpty()) {
                    Text("Episode provider belum tersedia.", Modifier.padding(horizontal = 18.dp), color = colors.onSurfaceVariant)
                } else {
                    LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(episodes.filter { it.number in episodeNumbers }, key = { it.id.ifBlank { it.number.toString() } }) { episode ->
                            PlayerEpisodeCard(episode, episode.number == episodeNumber, episode.number in watchedEpisodes) { onEpisodeClick(episode.number) }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
            }
            item {
                Surface(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.surfaceVariant.copy(alpha = .35f)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = colors.primary, modifier = Modifier.size(24.dp))
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

@Composable
private fun LandscapePlayerTopBar(title: String, episodeNumber: Int, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.Black.copy(alpha = .48f)) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowBack, "Kembali ke portrait", tint = Color.White) }
                Column(Modifier.padding(end = 14.dp)) {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Episode $episodeNumber", color = Color.White.copy(alpha = .72f), fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(999.dp), color = colors.primary.copy(alpha = .88f)) {
                    Text("KakaAnime", color = colors.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun LandscapeEpisodeRail(
    modifier: Modifier = Modifier,
    previousEpisode: Int,
    currentEpisode: Int,
    nextEpisode: Int,
    episodes: List<ProviderEpisode>,
    watchedEpisodes: Set<Int>,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onEpisodeClick: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val previous = episodes.firstOrNull { it.number == previousEpisode }
    val next = episodes.firstOrNull { it.number == nextEpisode }
    Row(
        modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 68.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LandscapeEpisodePreview(previous, previousEpisode, previousEpisode in watchedEpisodes, previousEpisode > 0, onPreviousEpisode)
        Surface(shape = RoundedCornerShape(999.dp), color = colors.surface.copy(alpha = .82f)) {
            Text("EP $currentEpisode", color = colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
        }
        LandscapeEpisodePreview(next, nextEpisode, nextEpisode in watchedEpisodes, true, onNextEpisode)
    }
}

@Composable
private fun LandscapeEpisodePreview(episode: ProviderEpisode?, number: Int, watched: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.width(150.dp).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = .58f),
    ) {
        Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 62.dp, height = 38.dp).clip(RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                AsyncImage(model = episode?.thumbnailUrl, contentDescription = "Episode $number", modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .48f)))
                if (!watched) Icon(Icons.Default.Lock, "Terkunci", tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f)) {
                Text("Episode $number", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (watched) "Sudah ditonton" else "Berikutnya", color = colors.primary.copy(alpha = .92f), fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PlayerQuickActions(quality: String, speed: Float, onQuality: () -> Unit, onSpeed: () -> Unit, onFullscreen: () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = false, onClick = onQuality, label = { Text(quality) }, leadingIcon = { Icon(Icons.Default.Settings, null, Modifier.size(18.dp)) })
        FilterChip(selected = false, onClick = onSpeed, label = { Text(String.format(Locale.getDefault(), "%.2gx", speed)) }, leadingIcon = { Icon(Icons.Default.Speed, null, Modifier.size(18.dp)) })
        FilterChip(selected = false, onClick = onFullscreen, label = { Text("Layar Penuh") }, leadingIcon = { Icon(Icons.Default.Fullscreen, null, Modifier.size(18.dp)) })
    }
}

@Composable
private fun PlayerEpisodeCard(episode: ProviderEpisode, selected: Boolean, watched: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.width(118.dp).clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            AsyncImage(model = episode.thumbnailUrl, contentDescription = "Episode ${episode.number}", modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = if (selected) .18f else .42f)))
            if (selected) {
                Surface(shape = RoundedCornerShape(999.dp), color = colors.primary) { Icon(Icons.Default.PlayArrow, null, tint = colors.onPrimary, modifier = Modifier.padding(7.dp).size(20.dp)) }
            } else if (!watched) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color.Black.copy(alpha = .65f)) { Icon(Icons.Default.Lock, "Terkunci", tint = Color.White, modifier = Modifier.padding(7.dp).size(18.dp)) }
            }
            if (watched && !selected) {
                Surface(Modifier.align(Alignment.TopEnd).padding(6.dp), shape = RoundedCornerShape(999.dp), color = colors.primary.copy(alpha = .9f)) { Text("✓", color = colors.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)) }
            }
            Surface(Modifier.align(Alignment.BottomStart).padding(6.dp), shape = RoundedCornerShape(7.dp), color = Color.Black.copy(alpha = .7f)) { Text("EP ${episode.number}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) }
        }
        Spacer(Modifier.height(6.dp))
        Text(episode.title?.takeIf { it.isNotBlank() } ?: "Episode ${episode.number}", fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) colors.primary else colors.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlayerUnlockOverlay(remainingSeconds: Int, onCancel: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val progress = remainingSeconds.coerceIn(0, 90) / 90f
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .78f)).clickable(onClick = {}), contentAlignment = Alignment.Center) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(24.dp), color = colors.surface.copy(alpha = .96f), tonalElevation = 6.dp) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, null, tint = colors.primary, modifier = Modifier.size(38.dp))
                Spacer(Modifier.height(8.dp))
                Text("Episode sedang dibuka", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("Tunggu sebentar. Setelah selesai, 2 diamond diberikan dan 1 diamond langsung digunakan.", textAlign = TextAlign.Center, color = colors.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(18.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), strokeWidth = 9.dp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format(Locale.getDefault(), "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("tersisa", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = colors.primaryContainer.copy(alpha = .35f)) { Text("Kalau iklan memberikan reward lebih dulu, timer ini langsung selesai dan episode terbuka.", Modifier.padding(12.dp), fontSize = 12.sp, color = colors.onSurface) }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
            }
        }
    }
}

@Composable
private fun PlayerSurface(player: Player) {
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
        update = { it.player = player },
    )
}

@Composable
private fun SpeedMenu(speed: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(1.dp)) {
        DropdownMenu(expanded = true, onDismissRequest = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
            listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { value ->
                DropdownMenuItem(text = { Text(if (value == speed) "✓ ${value}x" else "${value}x") }, onClick = { onSelect(value) })
            }
        }
    }
}
