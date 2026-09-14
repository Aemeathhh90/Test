package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.data.AniListMetadataService
import com.kakaanime.app.data.AnimeStateIdentity
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.SeasonAwareWatchHistoryEntry
import com.kakaanime.app.network.AnimeRepository

@Composable
fun ReDantotsuHomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onContinueWatchingClick: (Anime, Int) -> Unit = { anime, _ -> onAnimeClick(anime) },
    refreshKey: Int = 0,
    onAnimeCatalogLoaded: (List<Anime>) -> Unit = {},
) {
    val context = LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }
    val metadataService = remember { AniListMetadataService() }
    var backendAnime by remember(animeList) { mutableStateOf(animeList) }
    var watchHistory by remember(preferences) { mutableStateOf(preferences.loadWatchHistorySeasonAware()) }
    var posterUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }
    var diamonds by remember(preferences) { mutableIntStateOf(preferences.loadDiamonds()) }
    var isPremium by remember(preferences) { mutableStateOf(preferences.loadPremium()) }

    LaunchedEffect(animeList) {
        backendAnime = AnimeRepository.loadAnime(animeList)
        onAnimeCatalogLoaded(backendAnime)
        posterUrls = buildMap {
            backendAnime.forEach { anime -> metadataService.findByTitle(anime.title)?.imageUrl?.let { put(anime.title, it) } }
        }
    }
    LaunchedEffect(refreshKey) {
        watchHistory = preferences.loadWatchHistorySeasonAware()
        diamonds = preferences.loadDiamonds()
        isPremium = preferences.loadPremium()
    }

    val continueWatching = watchHistory.groupBy { it.animeGroupId to it.seasonNumber }.values
        .mapNotNull { it.maxByOrNull { h -> h.watchedAt } }
        .mapNotNull { history ->
            backendAnime.firstOrNull { anime ->
                val groupId = anime.animeGroupId.ifBlank { anime.title }
                val identity = AnimeStateIdentity(groupId, anime.seasonNumber, anime.seasonTitle)
                identity.animeGroupId == history.animeGroupId && identity.seasonNumber == history.seasonNumber &&
                    (identity.seasonTitle?.trim()?.equals(history.seasonTitle?.trim(), true) ?: history.seasonTitle.isNullOrBlank())
            }?.let { it to history.toWatchHistoryEntry() }
        }

    val query = searchQuery.trim()
    val filtered = backendAnime.filter { anime ->
        val text = query.isBlank() || anime.title.contains(query, true) || anime.genre.contains(query, true) || anime.searchAliases.any { it.contains(query, true) }
        val filter = when (selectedFilter) { "Ongoing" -> anime.status.equals("Ongoing", true); "Finished" -> anime.status.equals("Finished", true); else -> true }
        text && filter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { HomeTopBar(searchQuery, { searchQuery = it }, { showFilters = !showFilters }) }
        if (showFilters) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                items(listOf("All", "Ongoing", "Finished")) { f -> FilterChip(selected = selectedFilter == f, onClick = { selectedFilter = f }, label = { Text(f) }) }
            }
        }
        item { HomeProfileHeader(posterUrls[backendAnime.firstOrNull()?.title], diamonds, isPremium) }
        if (query.isBlank()) {
            if (continueWatching.isNotEmpty()) item { HomeEpisodeSection("Lanjut Nonton", continueWatching, onContinueWatchingClick) }
            item { HomeAnimeSection("New Updates", backendAnime.sortedByDescending { it.latestEpisode }, posterUrls, onAnimeClick, "NEW") }
            item { HomeAnimeSection("Trending Now", backendAnime, posterUrls, onAnimeClick) }
        } else item {
            if (filtered.isEmpty()) Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
                Text("Anime tidak ditemukan", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else HomeAnimeSection("Hasil Pencarian", filtered, posterUrls, onAnimeClick)
        }
    }
}

private fun SeasonAwareWatchHistoryEntry.toWatchHistoryEntry(): com.kakaanime.app.data.WatchHistoryEntry = com.kakaanime.app.data.WatchHistoryEntry(
    title = title.orEmpty(), episode = episode, episodeTitle = episodeTitle, episodeThumbnailUrl = episodeThumbnailUrl, watchedAt = watchedAt, durationMs = durationMs,
)

@Composable
private fun HomeTopBar(searchQuery: String, onSearchChange: (String) -> Unit, onFilterClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KakaAnime", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text("Watch Anime, Together.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HomeHeaderIcon(Icons.Outlined.Message, "Pesan")
            Spacer(Modifier.width(8.dp))
            Box {
                HomeHeaderIcon(Icons.Outlined.Notifications, "Notifikasi")
                Box(Modifier.align(Alignment.TopEnd).padding(5.dp).size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery, onValueChange = onSearchChange, modifier = Modifier.weight(1f), singleLine = true,
                shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Outlined.Search, "Cari") },
                placeholder = { Text("Cari anime, genre, atau studio...") },
            )
            Surface(Modifier.size(54.dp), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), onClick = onFilterClick) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Tune, "Filter") }
            }
        }
    }
}

@Composable
private fun HomeHeaderIcon(icon: ImageVector, description: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)) { Icon(icon, description, Modifier.padding(11.dp)) }
}

@Composable
private fun HomeProfileHeader(backgroundUrl: String?, diamonds: Int, isPremium: Boolean) {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .28f)) {
        Box(Modifier.fillMaxWidth().height(192.dp).clip(RoundedCornerShape(24.dp))) {
            if (backgroundUrl != null) AsyncImage(backgroundUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                MaterialTheme.colorScheme.background.copy(alpha = .18f),
                MaterialTheme.colorScheme.background.copy(alpha = .48f),
                MaterialTheme.colorScheme.background.copy(alpha = .96f),
            ))))
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                Surface(Modifier.size(70.dp), CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .90f), tonalElevation = 2.dp) {
                    Box(contentAlignment = Alignment.Center) { Text("KA", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Akun Saya", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        if (isPremium) ProfileBadge("Premium")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(if (isPremium) "Premium aktif" else "Akun Free", fontSize = 11.sp,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeShortcut("◆", diamonds.toString(), "Diamond", Modifier.weight(1f))
                HomeShortcut("♛", if (isPremium) "Premium" else "Free", if (isPremium) "1080p • Auto Skip" else "Upgrade untuk 1080p", Modifier.weight(1f))
                HomeShortcut("●●", "Watch Together", "Nonton bareng teman", Modifier.weight(1.12f))
            }
        }
    }
}

@Composable
private fun ProfileBadge(text: String) {
    Surface(RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HomeShortcut(icon: String, title: String, subtitle: String, modifier: Modifier) {
    Surface(modifier.height(62.dp), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .90f), tonalElevation = 1.dp) {
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
private fun HomeEpisodeSection(title: String, entries: List<Pair<Anime, com.kakaanime.app.data.WatchHistoryEntry>>, onClick: (Anime, Int) -> Unit) {
    Column {
        HomeSectionHeader(title)
        Spacer(Modifier.height(9.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(entries, key = { "${it.first.animeGroupId}::${it.first.seasonNumber ?: "na"}::${it.second.episode}" }) { (anime, history) ->
                Column(Modifier.width(136.dp).clickable { onClick(anime, history.episode) }) {
                    Box(Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        history.episodeThumbnailUrl?.let { AsyncImage(it, "${anime.title} Episode ${history.episode}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                        Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .72f)) {
                            Text("EP ${history.episode}", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(Modifier.height(6.dp)); Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(history.episodeTitle ?: "Episode ${history.episode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun HomeAnimeSection(title: String, animeList: List<Anime>, posterUrls: Map<String, String>, onClick: (Anime) -> Unit, badge: String? = null) {
    if (animeList.isEmpty()) return
    Column {
        HomeSectionHeader(title); Spacer(Modifier.height(9.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(animeList, key = { "${it.animeGroupId}::${it.seasonNumber ?: "na"}::${it.title.trim().lowercase()}" }) { anime ->
                Column(Modifier.width(136.dp).clickable { onClick(anime) }) {
                    Box(Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        val url = posterUrls[anime.title]
                        if (url != null) AsyncImage(url, anime.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text("POSTER", Modifier.align(Alignment.Center), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(62.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.scrim.copy(alpha = 0f), MaterialTheme.colorScheme.scrim.copy(alpha = .82f)))))
                        if (badge != null) Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .92f)) {
                            Text(badge, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(anime.title, Modifier.align(Alignment.BottomStart).padding(9.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1)
                    }
                    Spacer(Modifier.height(6.dp)); Text(anime.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("Ep ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}
