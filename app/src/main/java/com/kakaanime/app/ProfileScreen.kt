package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaThemeState

@Composable
fun ProfileScreen(
    themeState: KakaThemeState,
    monetizationState: MonetizationState,
    onPremiumClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }
    var editingProfile by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf(preferences.loadProfileName()) }
    var profileBio by remember { mutableStateOf(preferences.loadProfileBio()) }
    var avatarIndex by remember { mutableStateOf(preferences.loadProfileAvatarIndex()) }

    LaunchedEffect(preferences) {
        themeState.darkMode = preferences.loadDarkMode()
        themeState.accent = runCatching { KakaAccent.valueOf(preferences.loadAccentName()) }.getOrDefault(KakaAccent.Blue)
    }

    if (editingProfile) {
        EditProfileScreen(preferences) {
            profileName = preferences.loadProfileName()
            profileBio = preferences.loadProfileBio()
            avatarIndex = preferences.loadProfileAvatarIndex()
            editingProfile = false
        }
        return
    }

    val initials = profileName.trim().take(2).ifBlank { "KA" }.uppercase()
    val profileAccent = listOf(Color(0xFFFF4D67), Color(0xFF9C6BFF), Color(0xFF20C8E8), Color(0xFF35C98A))[avatarIndex.coerceIn(0, 3)]

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Manage your account and preferences", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Surface(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(68.dp).clip(CircleShape).background(profileAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, fontWeight = FontWeight.Black, fontSize = 19.sp, color = Color.White)
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(profileName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(profileBio, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (monetizationState.isPremium) Color(0xFFFFB52E).copy(alpha = .16f) else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    if (monetizationState.isPremium) "Premium" else "Free User",
                                    Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monetizationState.isPremium) Color(0xFFFFB52E) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { editingProfile = true }) {
                            Icon(Icons.Outlined.Edit, "Edit profile")
                        }
                    }

                    Surface(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background.copy(alpha = .55f)
                    ) {
                        Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("${monetizationState.diamonds} Diamonds", Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Premium", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onPremiumClick))
                        }
                    }
                }
            }
        }

        item {
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onPremiumClick),
                RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
            ) {
                Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (monetizationState.isPremium) "Premium Aktif" else "Upgrade to Premium", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("1080p, Auto Skip, dan fitur premium lainnya", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                AccountStat("Anime", preferences.loadWatchedEpisodes().size, Modifier.weight(1f))
                AccountStat("Favorite", preferences.loadFavoriteTitles().size, Modifier.weight(1f))
                AccountStat("Diamonds", monetizationState.diamonds, Modifier.weight(1f))
            }
        }

        item {
            AccountSection {
                AccountRow(Icons.Outlined.Person, "Profile", "View and edit your profile") { editingProfile = true }
                AccountRow(Icons.Outlined.Palette, "Appearance", "Theme and accent color") { showAppearance = true }
                AccountRow(Icons.Outlined.Info, "About KakaAnime", "Version, credits, and more") { showAbout = true }
            }
        }
    }

    if (showAppearance) {
        AlertDialog(
            onDismissRequest = { showAppearance = false },
            title = { Text("Appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DarkMode, null)
                        Spacer(Modifier.size(10.dp))
                        Text("Dark mode", Modifier.weight(1f))
                        Text(
                            if (themeState.darkMode) "ON" else "OFF",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                themeState.darkMode = !themeState.darkMode
                                preferences.saveDarkMode(themeState.darkMode)
                            }
                        )
                    }
                    Text("Accent color", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        KakaAccent.entries.forEach { accent ->
                            Surface(
                                Modifier.weight(1f).height(34.dp).clickable {
                                    themeState.accent = accent
                                    preferences.saveAccentName(accent.name)
                                },
                                RoundedCornerShape(9.dp),
                                color = accent.primary.copy(alpha = if (themeState.accent == accent) .92f else .24f)
                            ) {}
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAppearance = false }) { Text("Selesai") } }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About KakaAnime") },
            text = { Text("KakaAnime — anime always with you.\n\nCore UI follows the KakaAnime concept with ReDantotsu-inspired navigation, typography, spacing, and interaction patterns.") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun AccountSection(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)
    ) {
        Column(content = content, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun AccountRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AccountStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier, RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
