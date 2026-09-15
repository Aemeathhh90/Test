package com.kakaanime.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyProfileScreen(
    state: ProfileUiState,
    onEditProfile: () -> Unit = {},
    onAnimeClick: (String) -> Unit = {},
) = ProfileContent(state, onEditProfile, {}, onAnimeClick)

@Composable
fun OtherUserProfileScreen(
    state: ProfileUiState,
    onBack: () -> Unit = {},
    onFollowToggle: () -> Unit = {},
    onAnimeClick: (String) -> Unit = {},
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 30.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
            Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        ProfileContent(Modifier.weight(1f), state, {}, onFollowToggle, onAnimeClick)
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onAnimeClick: (String) -> Unit,
) = ProfileContent(Modifier.fillMaxSize(), state, onEditProfile, onFollowToggle, onAnimeClick)

@Composable
private fun ProfileContent(
    modifier: Modifier,
    state: ProfileUiState,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onAnimeClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Surface(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(72.dp), CircleShape, color = MaterialTheme.colorScheme.surface) {
                            Box(contentAlignment = Alignment.Center) { Text(state.username.take(2).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(state.username, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text(state.status, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        if (state.isSelf) {
                            IconButton(onClick = onEditProfile) { Icon(Icons.Outlined.Edit, "Edit profile") }
                        } else {
                            Button(onClick = onFollowToggle) { Text(if (state.isFollowing) "Following" else "Follow") }
                        }
                    }
                    if (state.bio.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(state.bio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileStat("Watched", state.watchedCount, Modifier.weight(1f))
                ProfileStat("Favorite", state.favoriteCount, Modifier.weight(1f))
                ProfileStat("Following", state.followingCount, Modifier.weight(1f))
            }
        }
        if (state.watchingTitles.isNotEmpty()) {
            item { Text("Watching Activity", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
            items(state.watchingTitles) { title ->
                ListRow(title, "Sedang ditonton", Icons.Outlined.PlayCircle) { onAnimeClick(title) }
            }
        }
        item { Text("Favorite Anime", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
        if (state.favorites.isEmpty()) {
            item { Text("Belum ada anime favorit.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.favorites, key = { it.id }) { anime ->
                ListRow(anime.title, "Episode ${anime.latestEpisode} • ★ ${anime.rating}", Icons.Outlined.Favorite) { onAnimeClick(anime.id) }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: Int, modifier: Modifier) {
    Surface(modifier, RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ListRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .30f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
