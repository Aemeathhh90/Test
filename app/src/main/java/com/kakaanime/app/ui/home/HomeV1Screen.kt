package com.kakaanime.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun HomeV1Screen(
    state: HomeUiState,
    onAnimeClick: (HomeAnimeUi) -> Unit = {},
    onContinueWatchingClick: (ContinueWatchingUi) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onDiamondClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    onWatchTogetherClick: () -> Unit = {},
) {
    var query by remember(state.searchQuery) { mutableStateOf(state.searchQuery) }
    var filter by remember(state.selectedFilter) { mutableStateOf(state.selectedFilter) }
    var showFilters by remember { mutableStateOf(false) }

    val filtered = remember(state.anime, query, filter) {
        state.anime.filter { anime ->
            val matchesQuery = query.isBlank() || anime.title.contains(query, true) || anime.genre.contains(query, true)
            val matchesFilter = when (filter) {
                HomeFilter.ALL -> true
                HomeFilter.ONGOING -> anime.status.equals("Ongoing", true)
                HomeFilter.FINISHED -> anime.status.equals("Finished", true)
            }
            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            HomeTopBar(query, { query = it }, showFilters, { showFilters = !showFilters }, onNotificationsClick)
        }
        if (showFilters) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HomeFilter.entries) { option ->
                        FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.label) })
                    }
                }
            }
        }
        item {
            HomeProfileHeader(state.username, state.diamonds, state.isPremium, onProfileClick, onDiamondClick, onPremiumClick, onWatchTogetherClick)
        }
        if (query.isBlank()) {
            if (state.continueWatching.isNotEmpty()) {
                item { HomeContinueSection(state.continueWatching, onContinueWatchingClick) }
            }
            item {
                HomeAnimeSection("New Updates", state.anime.filter { it.isNew }.ifEmpty { state.anime.sortedByDescending { it.latestEpisode } }, onAnimeClick, "NEW")
            }
            item { HomeAnimeSection("Trending Now", state.anime, onAnimeClick) }
        } else {
            item {
                if (filtered.isEmpty()) {
                    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
                        Text("Anime tidak ditemukan", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else HomeAnimeSection("Hasil Pencarian", filtered, onAnimeClick)
            }
        }
    }
}

@Composable
private fun HomeTopBar(query: String, onQueryChange: (String) -> Unit, showFilters: Boolean, onFilterClick: () -> Unit, onNotificationsClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KakaAnime", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("Watch Anime, Together.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(onClick = onNotificationsClick, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)) {
                Icon(Icons.Outlined.Notifications, "Notifikasi", Modifier.padding(11.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(query, onQueryChange, Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Outlined.Search, "Cari") }, placeholder = { Text("Cari anime, genre, atau studio...") })
            Surface(onClick = onFilterClick, Modifier.size(54.dp), RoundedCornerShape(16.dp), color = if (showFilters) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Tune, "Filter") }
            }
        }
    }
}

@Composable
private fun HomeProfileHeader(username: String, diamonds: Int, isPremium: Boolean, onProfileClick: () -> Unit, onDiamondClick: () -> Unit, onPremiumClick: () -> Unit, onWatchTogetherClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .28f)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.clickable(onClick = onProfileClick), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(58.dp), CircleShape, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Box(contentAlignment = Alignment.Center) { Text("KA", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(username, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        if (isPremium) Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) { Text("Premium", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    }
                    Text(if (isPremium) "Premium aktif" else "Akun Free", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeShortcut("◆", diamonds.toString(), "Diamond", Modifier.weight(1f), onDiamondClick)
                HomeShortcut("♛", if (isPremium) "1080p" else "Free", if (isPremium) "Auto Skip aktif" else "Upgrade", Modifier.weight(1f), onPremiumClick)
                HomeShortcut("●●", "Watch Together", "Nonton bareng", Modifier.weight(1.12f), onWatchTogetherClick)
            }
        }
    }
}

@Composable
private fun HomeShortcut(icon: String, title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(62.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text("Lihat semua  ›", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HomeContinueSection(entries: List<ContinueWatchingUi>, onClick: (ContinueWatchingUi) -> Unit) {
    Column {
        HomeSectionHeader("Lanjut Nonton")
        Spacer(Modifier.height(9.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(entries, key = { "${it.anime.id}:${it.episode}" }) { entry ->
                Column(Modifier.width(136.dp).clickable { onClick(entry) }) {
                    Box(Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (entry.thumbnailUrl != null) AsyncImage(entry.thumbnailUrl, entry.anime.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .72f)) { Text("EP ${entry.episode}", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(entry.anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(entry.episodeTitle ?: "Episode ${entry.episode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun HomeAnimeSection(title: String, anime: List<HomeAnimeUi>, onAnimeClick: (HomeAnimeUi) -> Unit, badge: String? = null) {
    if (anime.isEmpty()) return
    Column {
        HomeSectionHeader(title)
        Spacer(Modifier.height(9.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(anime, key = { it.id }) { item ->
                Column(Modifier.width(136.dp).clickable { onAnimeClick(item) }) {
                    Box(Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (item.posterUrl != null) AsyncImage(item.posterUrl, item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Text("POSTER", Modifier.align(Alignment.Center), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(62.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.scrim.copy(alpha = 0f), MaterialTheme.colorScheme.scrim.copy(alpha = .82f)))))
                        if (badge != null) Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .92f)) { Text(badge, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
                        Text(item.title, Modifier.align(Alignment.BottomStart).padding(9.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("Ep ${item.latestEpisode}  •  ★ ${item.rating}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}
