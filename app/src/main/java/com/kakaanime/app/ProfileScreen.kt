package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaThemeState

@Composable
fun ProfileScreen(themeState: KakaThemeState, monetizationState: MonetizationState, onPremiumClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { KakaAnimePreferences(context) }
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
    val gifImageLoader = remember { ImageLoader.Builder(context).components { add(GifDecoder.Factory()) }.build() }
    LaunchedEffect(Unit) { themeState.darkMode = prefs.loadDarkMode(); themeState.accent = runCatching { KakaAccent.valueOf(prefs.loadAccentName()) }.getOrDefault(KakaAccent.Blue) }
    if (showAnimeWatched) { AnimeWatchedScreen(prefs, localAnime, { showAnimeWatched = false }) { showAnimeWatched = false }; return }
    if (showEpisodeWatched) { EpisodeWatchedScreen(prefs, localAnime, { showEpisodeWatched = false }) { _, _ -> showEpisodeWatched = false }; return }
    if (showEdit) { EditProfileScreen(prefs, onBack = { name = prefs.loadProfileName(); bio = prefs.loadProfileBio(); avatar = prefs.loadProfileAvatarIndex(); profilePhotoUri = prefs.loadProfilePhotoUri(); bannerUri = prefs.loadProfileBannerUri(); premiumBannerUri = prefs.loadPremiumBannerUri(); animatedProfileUri = prefs.loadAnimatedProfileUri(); showEdit = false }, isPremium = monetizationState.isPremium, onPremiumClick = onPremiumClick); return }
    if (showSettings) { SettingsScreen(monetizationState = monetizationState, onBack = { showSettings = false }, onPremiumClick = onPremiumClick); return }
    val initials = name.trim().take(2).ifBlank { "KA" }.uppercase()
    val avatarColor = listOf(Color(0xFFFF4D67), Color(0xFF9C6BFF), Color(0xFF20C8E8), Color(0xFF35C98A))[avatar.coerceIn(0, 3)]
    val watchHistory = prefs.loadWatchHistory(); val lastWatched = prefs.loadWatchedEpisodes(); val animeWatched = (watchHistory.map { it.title }.toSet() + lastWatched.keys).size; val episodeWatched = watchHistory.size; val favorites = prefs.loadFavoriteTitles().size
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 116.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Account", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Manage your account and preferences", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(onClick = { showSettings = true }) { Icon(Icons.Outlined.Settings, "Settings") } } }
        item { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(18.dp))) { if (bannerUri != null) AsyncImage(model = bannerUri, contentDescription = "Banner Atas", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop) else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(avatarColor.copy(alpha = .85f), MaterialTheme.colorScheme.primary.copy(alpha = .58f), Color(0xFF111827))))); Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) { Text("KakaAnime Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("Banner Atas", fontSize = 10.sp, color = Color.White.copy(alpha = .78f)) } }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(68.dp).clip(CircleShape).background(avatarColor), contentAlignment = Alignment.Center) { when { monetizationState.isPremium && animatedProfileUri != null -> AsyncImage(model = animatedProfileUri, imageLoader = gifImageLoader, contentDescription = "Animated profile", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop); profilePhotoUri != null -> AsyncImage(model = profilePhotoUri, contentDescription = "Foto profil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop); else -> Text(initials, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White) } }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(bio, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2); Spacer(Modifier.height(5.dp)); Text(if (monetizationState.isPremium) "Premium" else "Free User", fontSize = 10.sp, fontWeight = FontWeight.Bold) }; IconButton(onClick = { showEdit = true }) { Icon(Icons.Outlined.Edit, "Edit profile") } }
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background.copy(alpha = .55f), RoundedCornerShape(14.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("${monetizationState.diamonds} Diamonds", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("Premium", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onPremiumClick)) }
            if (monetizationState.isPremium && premiumBannerUri != null) AsyncImage(model = premiumBannerUri, contentDescription = "Premium Banner", modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(14.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        } } }
        item { Surface(Modifier.fillMaxWidth().clickable(onClick = onPremiumClick), RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(if (monetizationState.isPremium) "Premium Aktif" else "Upgrade to Premium", fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("1080p, Auto Skip, dan fitur premium lainnya", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("›", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Stat("Anime Watched", animeWatched, Modifier.weight(1f)) { showAnimeWatched = true }; Stat("Episode Watched", episodeWatched, Modifier.weight(1f)) { showEpisodeWatched = true }; Stat("Favorites", favorites, Modifier.weight(1f)) {} } }
        item { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) { Column(Modifier.padding(vertical = 4.dp)) { AccountRow(Icons.Outlined.Settings, "Settings", "App preferences and data") { showSettings = true }; AccountRow(Icons.Outlined.Person, "Profile", "View and edit your profile") { showEdit = true }; AccountRow(Icons.Outlined.Palette, "Appearance", "Theme and accent color") { showAppearance = true }; AccountRow(Icons.Outlined.Star, "Premium", "Manage premium and diamonds") { onPremiumClick() }; AccountRow(Icons.Outlined.Notifications, "Notifications", "Episode updates and system notifications") { showNotifications = true }; AccountRow(Icons.Outlined.Info, "About KakaAnime", "Version, credits, and more") { showAbout = true } } } }
    }
    if (showAppearance) AlertDialog(onDismissRequest = { showAppearance = false }, title = { Text("Appearance") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Dark mode: ${if (themeState.darkMode) "ON" else "OFF"}", modifier = Modifier.clickable { themeState.darkMode = !themeState.darkMode; prefs.saveDarkMode(themeState.darkMode) }); Text("Accent color", fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { KakaAccent.entries.forEach { a -> Surface(Modifier.weight(1f).height(34.dp).clickable { themeState.accent = a; prefs.saveAccentName(a.name) }, RoundedCornerShape(8.dp), color = a.primary.copy(alpha = if (themeState.accent == a) .9f else .25f)) {} } } } }, confirmButton = { TextButton(onClick = { showAppearance = false }) { Text("Selesai") } })
    if (showNotifications) SimpleDialog("Notifications", "Notifikasi episode baru dan notifikasi sistem akan ditempatkan di sini.") { showNotifications = false }
    if (showAbout) SimpleDialog("About KakaAnime", "KakaAnime — anime always with you.\n\nUI/UX menggunakan arah KakaAnime dengan pola ReDantotsu.") { showAbout = false }
}
@Composable private fun AccountRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("›", fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun Stat(label: String, value: Int, modifier: Modifier, onClick: () -> Unit) { Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) { Column(Modifier.padding(horizontal = 5.dp, vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun SimpleDialog(title: String, message: String, onClose: () -> Unit) { AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onClose) { Text("Tutup") } }) }
