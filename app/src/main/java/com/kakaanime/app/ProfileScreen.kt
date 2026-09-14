package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.SeasonAwareWatchHistoryEntry
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.network.AnimeRepository
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaThemeState

@Composable
fun ProfileScreen(
    themeState: KakaThemeState,
    monetizationState: MonetizationState,
    onPremiumClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onAnimeClick: (Anime) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { KakaAnimePreferences(context) }
    var profileAnime by remember { mutableStateOf(localAnime) }
    var showAnimeWatched by remember { mutableStateOf(false) }
    var showEpisodeWatched by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(prefs.loadProfileName()) }
    var bio by remember { mutableStateOf(prefs.loadProfileBio()) }
    var avatar by remember { mutableStateOf(prefs.loadProfileAvatarIndex()) }
    var profilePhotoUri by remember { mutableStateOf(prefs.loadProfilePhotoUri()) }
    var bannerUri by remember { mutableStateOf(prefs.loadProfileBannerUri()) }
    var premiumBannerUri by remember { mutableStateOf(prefs.loadPremiumBannerUri()) }
    var animatedProfileUri by remember { mutableStateOf(prefs.loadAnimatedProfileUri()) }
    var searchQuery by remember { mutableStateOf("") }
    var profileFilter by remember { mutableStateOf("Semua") }

    val gifImageLoader = remember { ImageLoader.Builder(context).components { add(GifDecoder.Factory()) }.build() }

    LaunchedEffect(Unit) {
        themeState.darkMode = prefs.loadDarkMode()
        themeState.accent = runCatching { KakaAccent.valueOf(prefs.loadAccentName()) }.getOrDefault(KakaAccent.Blue)
        profileAnime = runCatching { AnimeRepository.loadAnime(localAnime) }.getOrDefault(localAnime)
        val legacyTitles = prefs.loadFavoriteTitles()
        if (legacyTitles.isNotEmpty()) {
            val uniqueGroupByTitle = profileAnime.groupBy { it.title.trim() }
                .mapNotNull { (title, matches) ->
                    val ids = matches.map { it.animeGroupId.ifBlank { it.title }.trim() }.distinct()
                    if (ids.size == 1) title to ids.first() else null
                }.toMap()
            val migrated = legacyTitles.mapNotNull { uniqueGroupByTitle[it.trim()] }.toSet()
            if (migrated.isNotEmpty()) prefs.saveFavoriteGroupIds(prefs.loadFavoriteGroupIds() + migrated)
            if (migrated.size == legacyTitles.size) prefs.saveFavoriteTitles(emptySet())
        }
    }

    if (showAnimeWatched) { AnimeWatchedScreen(prefs, profileAnime, { showAnimeWatched = false }) { showAnimeWatched = false }; return }
    if (showEpisodeWatched) { EpisodeWatchedScreen(prefs, profileAnime, { showEpisodeWatched = false }) { _, _ -> showEpisodeWatched = false }; return }
    if (showEdit) { EditProfileScreen(prefs, { name = prefs.loadProfileName(); bio = prefs.loadProfileBio(); avatar = prefs.loadProfileAvatarIndex(); profilePhotoUri = prefs.loadProfilePhotoUri(); bannerUri = prefs.loadProfileBannerUri(); premiumBannerUri = prefs.loadPremiumBannerUri(); animatedProfileUri = prefs.loadAnimatedProfileUri(); showEdit = false }, monetizationState.isPremium, onPremiumClick); return }
    if (showSettings) { SettingsScreen(monetizationState, { showSettings = false }, onPremiumClick); return }

    val initials = name.trim().take(2).ifBlank { "KA" }.uppercase()
    val avatarColor = listOf(Color(0xFFFF4D67), Color(0xFF9C6BFF), Color(0xFF20C8E8), Color(0xFF35C98A))[avatar.coerceIn(0, 3)]
    val history = prefs.loadWatchHistorySeasonAware()
    val watchedAnimeCount = history.map { it.animeGroupId }.distinct().size
    val watchedEpisodeCount = history.size
    val favoriteCount = prefs.loadFavoriteGroupIds().size
    val continueItems = history.distinctBy { "${it.animeGroupId}::${it.seasonNumber}::${it.seasonTitle}::${it.episode}" }
        .filter { entry ->
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() || entry.title.orEmpty().contains(q, true) || entry.episodeTitle.orEmpty().contains(q, true) || entry.episode.toString().contains(q)
            val groupId = entry.animeGroupId.trim()
            val isFavorite = prefs.isFavoriteGroup(groupId)
            val matchesFilter = when (profileFilter) {
                "Favorites" -> isFavorite
                "Terbaru" -> true
                else -> true
            }
            matchesSearch && matchesFilter
        }
        .sortedByDescending { it.watchedAt }
        .take(12)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Akun dan aktivitas anime kamu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showSettings = true }) { Icon(Icons.Outlined.Settings, "Settings") }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(19.dp))) {
                        if (bannerUri != null) {
                            AsyncImage(bannerUri, "Banner profil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(avatarColor.copy(alpha = .82f), MaterialTheme.colorScheme.primary.copy(alpha = .62f), Color(0xFF101827)))))
                        }
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = .18f)))
                        Row(Modifier.align(Alignment.BottomStart).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(70.dp).clip(CircleShape).background(avatarColor), contentAlignment = Alignment.Center) {
                                when {
                                    monetizationState.isPremium && animatedProfileUri != null -> AsyncImage(animatedProfileUri, "Animated profile", Modifier.fillMaxSize(), imageLoader = gifImageLoader, contentScale = ContentScale.Crop)
                                    profilePhotoUri != null -> AsyncImage(profilePhotoUri, "Foto profil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    else -> Text(initials, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(name, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(if (monetizationState.isPremium) "Premium" else "Free User", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = .82f))
                            }
                        }
                        IconButton(onClick = { showEdit = true }, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp)) {
                            Icon(Icons.Outlined.Edit, "Edit profile", tint = Color.White)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileQuickCard(Icons.Outlined.Diamond, "${monetizationState.diamonds}", "Diamond") {}
                        ProfileQuickCard(Icons.Outlined.Star, if (monetizationState.isPremium) "Premium" else "Free", "Membership") { onPremiumClick() }
                        ProfileQuickCard(Icons.Outlined.Groups, "Watch", "Together") { }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text("Cari anime, episode, atau aktivitas...") }
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aktivitas", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAnimeWatched = true }) { Icon(Icons.Outlined.Tv, "Anime watched") }
                IconButton(onClick = { showEpisodeWatched = true }) { Icon(Icons.Outlined.History, "Episode watched") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Semua", "Terbaru", "Favorites").forEach { filter ->
                    FilterChip(selected = profileFilter == filter, onClick = { profileFilter = filter }, label = { Text(filter, fontSize = 11.sp) })
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lanjut Nonton", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${continueItems.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (continueItems.isEmpty()) {
            item {
                ProfileEmptyCard(
                    if (searchQuery.isBlank()) "Belum ada riwayat tontonan" else "Tidak ada hasil",
                    if (searchQuery.isBlank()) "Anime yang kamu mulai tonton akan muncul di sini." else "Coba kata kunci atau filter lain."
                )
            }
        } else {
            items(continueItems, key = { "${it.animeGroupId}:${it.seasonNumber}:${it.seasonTitle}:${it.episode}" }) { entry ->
                val targetAnime = profileAnime.firstOrNull { it.animeGroupId.ifBlank { it.title }.trim() == entry.animeGroupId.trim() && (entry.seasonNumber == null || it.seasonNumber == entry.seasonNumber) } ?: profileAnime.firstOrNull { it.title.equals(entry.title, true) }
                ProfileContinueCard(entry, onClick = { targetAnime?.let(onAnimeClick) })
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Anime", watchedAnimeCount, Modifier.weight(1f)) { showAnimeWatched = true }
                Stat("Episode", watchedEpisodeCount, Modifier.weight(1f)) { showEpisodeWatched = true }
                Stat("Favorite", favoriteCount, Modifier.weight(1f)) { onFavoriteClick() }
            }
        }

        item {
            Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .40f)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    AccountRow(Icons.Outlined.Settings, "Settings", "App preferences and data") { showSettings = true }
                    AccountRow(Icons.Outlined.Person, "Edit Profile", "Foto, banner, nama, dan bio") { showEdit = true }
                    AccountRow(Icons.Outlined.Palette, "Appearance", "Theme dan accent color") { showAppearance = true }
                    AccountRow(Icons.Outlined.Star, "Premium", "1080p, Auto Skip, dan fitur premium") { onPremiumClick() }
                    AccountRow(Icons.Outlined.Notifications, "Notifications", "Episode updates dan notifikasi sistem") { showNotifications = true }
                    AccountRow(Icons.Outlined.Info, "About KakaAnime", "Versi dan informasi aplikasi") { showAbout = true }
                }
            }
        }
    }

    if (showAppearance) {
        AlertDialog(onDismissRequest = { showAppearance = false }, title = { Text("Appearance") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dark mode: ${if (themeState.darkMode) "ON" else "OFF"}", Modifier.clickable { themeState.darkMode = !themeState.darkMode; prefs.saveDarkMode(themeState.darkMode) })
                Text("Accent color", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { KakaAccent.entries.forEach { a -> Surface(Modifier.weight(1f).height(34.dp).clickable { themeState.accent = a; prefs.saveAccentName(a.name) }, RoundedCornerShape(8.dp), color = a.primary.copy(alpha = if (themeState.accent == a) .9f else .25f)) {} } }
            }
        }, confirmButton = { TextButton(onClick = { showAppearance = false }) { Text("Selesai") } })
    }
    if (showNotifications) SimpleDialog("Notifications", "Notifikasi episode baru dan notifikasi sistem akan ditempatkan di sini.") { showNotifications = false }
    if (showAbout) SimpleDialog("About KakaAnime", "KakaAnime — anime always with you.") { showAbout = false }
}

@Composable private fun ProfileQuickCard(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, onClick: () -> Unit) {
    Surface(Modifier.weight(1f).height(66.dp).clickable(onClick = onClick), RoundedCornerShape(17.dp), color = MaterialTheme.colorScheme.background.copy(alpha = .42f)) {
        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable private fun ProfileContinueCard(entry: SeasonAwareWatchHistoryEntry, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(19.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .44f)) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(112.dp).height(68.dp).clip(RoundedCornerShape(13.dp))) {
                if (!entry.episodeThumbnailUrl.isNullOrBlank()) AsyncImage(entry.episodeThumbnailUrl, "Episode ${entry.episode}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = .22f)))
                Box(Modifier.align(Alignment.BottomStart).padding(6.dp).clip(RoundedCornerShape(7.dp)).background(MaterialTheme.colorScheme.scrim.copy(alpha = .72f)).padding(horizontal = 5.dp, vertical = 2.dp)) { Text("EP ${entry.episode}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(entry.title ?: "Anime", fontWeight = FontWeight.Bold, maxLines = 1)
                Text(entry.episodeTitle ?: "Episode ${entry.episode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(28.dp).height(3.dp).clip(RoundedCornerShape(99.dp)).background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.width(5.dp))
                    Text("Lanjutkan", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.Outlined.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable private fun AccountRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text("›", fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun Stat(label: String, value: Int, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(horizontal = 5.dp, vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun ProfileEmptyCard(title: String, message: String) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun SimpleDialog(title: String, message: String, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onClose) { Text("Tutup") } })
}
