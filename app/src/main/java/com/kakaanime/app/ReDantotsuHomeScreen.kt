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

    // Keep account summary truthful. The old Home hero hard-coded a Premium
    // user and a diamond balance, which made the UI disagree with Preferences.
    var diamonds by remember(preferences) { mutableIntStateOf(preferences.loadDiamonds()) }
    var isPremium by remember(preferences) { mutableStateOf(preferences.loadPremium()) }

    LaunchedEffect(animeList) {
        backendAnime = AnimeRepository.loadAnime(animeList)
        onAnimeCatalogLoaded(backendAnime)
        posterUrls = buildMap {
            backendAnime.forEach { anime ->
                metadataService.findByTitle(anime.title)?.imageUrl?.let { put(anime.title, it) }
            }
        }
    }
    LaunchedEffect(refreshKey) {
        watchHistory = preferences.loadWatchHistorySeasonAware()
        diamonds = preferences.loadDiamonds()
        isPremium = preferences.loadPremium()
    }

    val profileBackground = posterUrls[backendAnime.firstOrNull()?.title]
    val continueWatching = watchHistory
        .groupBy { it.animeGroupId to it.seasonNumber }
        .values
        .mapNotNull { it.maxByOrNull { history -> history.watchedAt } }
        .mapNotNull { history ->
            backendAnime.firstOrNull { anime ->
                val groupId = anime.animeGroupId.ifBlank { anime.title }
                val identity = AnimeStateIdentity(groupId, anime.seasonNumber, anime.seasonTitle)
                identity.animeGroupId == history.animeGroupId &&
                    identity.seasonNumber == history.seasonNumber &&
                    (identity.seasonTitle?.trim()?.equals(history.seasonTitle?.trim(), true) ?: history.seasonTitle.isNullOrBlank())
            }?.let { anime -> anime to history.toWatchHistoryEntry() }
        }

    val normalizedQuery = searchQuery.trim()
    val filteredAnime = backendAnime.filter { anime ->
        val matchesQuery = normalizedQuery.isBlank() ||
            anime.title.contains(normalizedQuery, true) ||
            anime.genre.contains(normalizedQuery, true) ||
            anime.searchAliases.any { it.contains(normalizedQuery, true) }
        val matchesFilter = when (selectedFilter) {
            "Ongoing" -> anime.status.equals("Ongoing", true)
            "Finished" -> anime.status.equals("Finished", true)
            else -> true
        }
        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeTopBar(searchQuery, { searchQuery = it }, { showFilters = !showFilters }) }
        if (showFilters) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
                    items(listOf("All", "Ongoing", "Finished")) { filter ->
                        FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) })
                    }
                }
            }
        }
        item { HomeProfileHeader(profileBackground, diamonds = diamonds, isPremium = isPremium) }
        if (normalizedQuery.isBlank()) {
            if (continueWatching.isNotEmpty()) item { HomeEpisodeSection("Lanjut Nonton", continueWatching, onContinueWatchingClick) }
            item { HomeAnimeSection("New Updates", backendAnime.sortedByDescending { it.latestEpisode }, posterUrls, onAnimeClick, "NEW") }
            item { HomeAnimeSection("Anime Musiman", backendAnime, posterUrls, onAnimeClick, "SEASONAL") }
            item { HomeAnimeSection("Trending Now", backendAnime, posterUrls, onAnimeClick) }
            item { HomeAnimeSection("Anime Completed", backendAnime.filter { it.status.equals("Finished", true) }, posterUrls, onAnimeClick, "COMPLETED") }
            item { HomeAnimeSection("Recommended", backendAnime.reversed(), posterUrls, onAnimeClick) }
        } else {
            item {
                if (filteredAnime.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                    ) {
                        Text("Anime tidak ditemukan", modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    HomeAnimeSection("Hasil Pencarian", filteredAnime, posterUrls, onAnimeClick)
                }
            }
        }
    }
}

private fun SeasonAwareWatchHistoryEntry.toWatchHistoryEntry(): com.kakaanime.app.data.WatchHistoryEntry =
    com.kakaanime.app.data.WatchHistoryEntry(
        title = title.orEmpty(), episode = episode, episodeTitle = episodeTitle,
        episodeThumbnailUrl = episodeThumbnailUrl, watchedAt = watchedAt, durationMs = durationMs,
    )

@Composable
private fun HomeTopBar(searchQuery: String, onSearchChange: (String) -> Unit, onFilterClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("KakaAnime", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Watch Anime, Together.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HomeHeaderIcon(Icons.Outlined.Message, "Pesan")
            Spacer(Modifier.width(8.dp))
            Box {
                HomeHeaderIcon(Icons.Outlined.Notifications, "Notifikasi")
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(7.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, "Cari") },
                placeholder = { Text("Cari anime...") },
            )
            IconButton(onClick = onFilterClick) { Icon(Icons.Outlined.Tune, "Filter") }
        }
    }
}

@Composable
private fun HomeHeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f),
    ) {
        Icon(icon, description, modifier = Modifier.padding(11.dp))
    }
}

@Composable
private fun HomeProfileHeader(backgroundUrl: String?, diamonds: Int, isPremium: Boolean) {
    Surface(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .28f),
    ) {
        Box(Modifier.fillMaxWidth().height(228.dp).clip(RoundedCornerShape(24.dp))) {
            if (backgroundUrl != null) {
                AsyncImage(backgroundUrl, null, Modifier.fillMaxWidth().height(142.dp), contentScale = ContentScale.Crop)
                Box(
                    Modifier.fillMaxWidth().height(142.dp).background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = .92f),
                                MaterialTheme.colorScheme.background.copy(alpha = .42f),
                                MaterialTheme.colorScheme.background.copy(alpha = .86f),
                            )
                        )
                    )
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(142.dp).background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = .10f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = .16f),
                            )
                        )
                    )
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    Modifier.size(78.dp),
                    CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("KA", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Akun Saya", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        if (isPremium) ProfileBadge("Premium")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isPremium) "Premium aktif" else "Akun Free",
                        fontSize = 11.sp,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    ProfileBadge("Lv. 1")
                }
            }

            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HomeShortcut("💎", diamonds.toString(), "Diamond", Modifier.weight(1f))
                HomeShortcut("♛", if (isPremium) "Premium" else "Free", if (isPremium) "1080p • Auto Skip" else "Upgrade untuk 1080p", Modifier.weight(1f))
                HomeShortcut("●●", "Watch Together", "Nonton bareng", Modifier.weight(1.15f))
            }
        }
    }
}

@Composable
private fun ProfileBadge(text: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HomeShortcut(icon: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(modifier, RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .82f)) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 17.sp)
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HomeEpisodeSection(title: String, entries: List<Pair<Anime, com.kakaanime.app.data.WatchHistoryEntry>>, onClick: (Anime, Int) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Lihat semua", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(entries, key = { "${it.first.animeGroupId}::season:${it.first.seasonNumber ?: "na"}::title:${it.first.seasonTitle?.trim()?.lowercase().orEmpty()}::episode:${it.second.episode}" }) { (anime, history) ->
                HomeEpisodeCard(anime, history, onClick)
            }
        }
    }
}

@Composable
private fun HomeEpisodeCard(anime: Anime, history: com.kakaanime.app.data.WatchHistoryEntry, onClick: (Anime, Int) -> Unit) {
    Column(Modifier.width(136.dp).clickable { onClick(anime, history.episode) }) {
        Box(Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            history.episodeThumbnailUrl?.let { thumbnail ->
                AsyncImage(thumbnail, "${anime.title} Episode ${history.episode}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .72f)) {
                Text("EP ${history.episode}", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.height(7.dp))
        Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(history.episodeTitle ?: "Episode ${history.episode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun HomeAnimeSection(title: String, animeList: List<Anime>, posterUrls: Map<String, String>, onClick: (Anime) -> Unit, badge: String? = null) {
    if (animeList.isEmpty()) return
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Lihat semua", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(animeList, key = { "${it.animeGroupId}::season:${it.seasonNumber ?: "na"}::title:${it.seasonTitle?.trim()?.lowercase().orEmpty()}::anime:${it.title.trim().lowercase()}" }) { anime ->
                ReferencePosterCard(anime, posterUrls[anime.title], onClick, badge)
            }
        }
    }
}

@Composable
private fun ReferencePosterCard(anime: Anime, posterUrl: String?, onClick: (Anime) -> Unit, badge: String?) {
    Column(Modifier.width(136.dp).clickable { onClick(anime) }) {
        Box(Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (posterUrl != null) {
                AsyncImage(posterUrl, anime.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .12f), MaterialTheme.colorScheme.surface))))
                Text("POSTER", Modifier.align(Alignment.Center), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (badge != null) {
                Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .9f)) {
                    Text(badge, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("Ep ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
