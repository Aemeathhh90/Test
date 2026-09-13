package com.kakaanime.app

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.data.AniListPosterService
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.WatchHistoryEntry

private enum class WatchLayout { GRID, LIST }
private enum class AnimeWatchedFilter { ALL, IN_PROGRESS, COMPLETED, DROPPED }
private enum class EpisodeWatchedFilter { ALL, TODAY, WEEK, MONTH }

@Composable
fun AnimeWatchedScreen(
    preferences: KakaAnimePreferences,
    animeList: List<Anime>,
    onBack: () -> Unit,
    onAnimeClick: (Anime) -> Unit
) {
    var layout by remember { mutableStateOf(WatchLayout.GRID) }
    var filter by remember { mutableStateOf(AnimeWatchedFilter.ALL) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Last Watched") }
    var sortMenu by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val history = preferences.loadWatchHistory()
    val watched = preferences.loadWatchedEpisodes()

    val records = history
        .groupBy { it.title }
        .mapValues { (_, entries) -> entries.maxByOrNull { it.watchedAt }!! }
        .values
        .mapNotNull { entry ->
            animeList.firstOrNull { it.title.equals(entry.title, ignoreCase = true) }?.let { anime -> anime to entry }
                ?: animeList.firstOrNull { it.title.equals(entry.title, ignoreCase = true) }?.let { anime -> anime to entry }
        }
        .ifEmpty {
            watched.mapNotNull { (title, episode) -> animeList.firstOrNull { it.title.equals(title, true) }?.let { it to WatchHistoryEntry(title, episode, null, 0L, 0L) } }
        }

    val filtered = records
        .filter { (anime, entry) ->
            val status = animeWatchStatus(anime, entry.episode)
            (filter == AnimeWatchedFilter.ALL || status == filter) &&
                (query.isBlank() || anime.title.contains(query, true) || anime.genre.contains(query, true))
        }
        .let { list -> if (sort == "Last Watched") list.sortedByDescending { it.second.watchedAt } else list.sortedBy { it.first.title.lowercase() } }

    LaunchedEffect(records.map { it.first.title }) {
        posters = AniListPosterService().getPosterUrls(records.map { it.first.title })
    }

    WatchHistoryScaffold(
        title = "Anime Watched",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        layout = layout,
        onLayoutChange = { layout = it },
        sortLabel = sort,
        onSortClick = { sortMenu = true },
        sortMenu = {
            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                listOf("Last Watched", "Title").forEach { value ->
                    DropdownMenuItem(text = { Text(value) }, onClick = { sort = value; sortMenu = false })
                }
            }
        },
        filterContent = {
            listOf(
                AnimeWatchedFilter.ALL to "All",
                AnimeWatchedFilter.IN_PROGRESS to "In Progress",
                AnimeWatchedFilter.COMPLETED to "Completed",
                AnimeWatchedFilter.DROPPED to "Dropped"
            ).forEach { (value, label) ->
                FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(label, fontSize = 11.sp) })
            }
        },
        total = filtered.size,
        content = {
            if (layout == WatchLayout.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.first.title }) { (anime, entry) ->
                        AnimeWatchedGridCard(anime, entry, posters[anime.title]) { onAnimeClick(anime) }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.first.title }) { (anime, entry) ->
                        AnimeWatchedListCard(anime, entry, posters[anime.title]) { onAnimeClick(anime) }
                    }
                }
            }
        }
    )
}

@Composable
fun EpisodeWatchedScreen(
    preferences: KakaAnimePreferences,
    animeList: List<Anime>,
    onBack: () -> Unit,
    onEpisodeClick: (Anime, Int) -> Unit
) {
    var layout by remember { mutableStateOf(WatchLayout.GRID) }
    var filter by remember { mutableStateOf(EpisodeWatchedFilter.ALL) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Newest") }
    var sortMenu by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val history = preferences.loadWatchHistory()
    val now = System.currentTimeMillis()

    val filtered = history
        .filter { entry ->
            val age = if (entry.watchedAt > 0L) now - entry.watchedAt else Long.MAX_VALUE
            val timeMatch = when (filter) {
                EpisodeWatchedFilter.ALL -> true
                EpisodeWatchedFilter.TODAY -> age <= 24L * 60L * 60L * 1000L
                EpisodeWatchedFilter.WEEK -> age <= 7L * 24L * 60L * 60L * 1000L
                EpisodeWatchedFilter.MONTH -> age <= 31L * 24L * 60L * 60L * 1000L
            }
            timeMatch && (query.isBlank() || entry.title.contains(query, true) || (entry.episodeTitle?.contains(query, true) == true))
        }
        .let { list -> if (sort == "Newest") list.sortedByDescending { it.watchedAt } else list.sortedBy { it.title.lowercase() } }

    LaunchedEffect(filtered.map { it.title }) {
        posters = AniListPosterService().getPosterUrls(filtered.map { it.title })
    }

    WatchHistoryScaffold(
        title = "Episode Watched",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        layout = layout,
        onLayoutChange = { layout = it },
        sortLabel = sort,
        onSortClick = { sortMenu = true },
        sortMenu = {
            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                listOf("Newest", "Title").forEach { value ->
                    DropdownMenuItem(text = { Text(value) }, onClick = { sort = value; sortMenu = false })
                }
            }
        },
        filterContent = {
            listOf(
                EpisodeWatchedFilter.ALL to "All",
                EpisodeWatchedFilter.TODAY to "Today",
                EpisodeWatchedFilter.WEEK to "This Week",
                EpisodeWatchedFilter.MONTH to "This Month"
            ).forEach { (value, label) ->
                FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(label, fontSize = 11.sp) })
            }
        },
        total = filtered.size,
        content = {
            if (layout == WatchLayout.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { "${it.title}-${it.episode}" }) { entry ->
                        EpisodeWatchedGridCard(entry, posters[entry.title]) {
                            animeList.firstOrNull { it.title.equals(entry.title, true) }?.let { onEpisodeClick(it, entry.episode) }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { "${it.title}-${it.episode}" }) { entry ->
                        EpisodeWatchedListCard(entry, posters[entry.title]) {
                            animeList.firstOrNull { it.title.equals(entry.title, true) }?.let { onEpisodeClick(it, entry.episode) }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun WatchHistoryScaffold(
    title: String,
    onBack: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    layout: WatchLayout,
    onLayoutChange: (WatchLayout) -> Unit,
    sortLabel: String,
    onSortClick: () -> Unit,
    sortMenu: @Composable () -> Unit,
    filterContent: @Composable RowScope.() -> Unit,
    total: Int,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { }) { Icon(Icons.Outlined.MoreVert, "More") }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).height(52.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text("Search", fontSize = 12.sp) }
            )
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollStateCompat()).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            content = filterContent
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Sort, null, modifier = Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(7.dp))
            Text("Sort: $sortLabel", fontSize = 12.sp, modifier = Modifier.clickable(onClick = onSortClick))
            Spacer(Modifier.weight(1f))
            Text("Total: $total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = { onLayoutChange(if (layout == WatchLayout.GRID) WatchLayout.LIST else WatchLayout.GRID) }, modifier = Modifier.size(36.dp)) {
                Icon(if (layout == WatchLayout.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView, "Toggle layout")
            }
            sortMenu()
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun AnimeWatchedGridCard(anime: Anime, entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    val progress = if (anime.latestEpisode > 0) (entry.episode.toFloat() / anime.latestEpisode).coerceIn(0f, 1f) else 0f
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(4.dp)) {
            PosterImage(poster, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
            Text(anime.title, Modifier.padding(horizontal = 3.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${entry.episode} / ${anime.latestEpisode}", Modifier.padding(horizontal = 3.dp), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProgressBar(progress, Modifier.padding(horizontal = 3.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun AnimeWatchedListCard(anime: Anime, entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    val progress = if (anime.latestEpisode > 0) (entry.episode.toFloat() / anime.latestEpisode).coerceIn(0f, 1f) else 0f
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterImage(poster, Modifier.size(width = 82.dp, height = 112.dp))
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(anime.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(anime.genre, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Text("Episode ${entry.episode} / ${anime.latestEpisode}", fontSize = 12.sp)
                ProgressBar(progress, Modifier.padding(vertical = 6.dp))
                Text(statusLabel(anime, entry.episode) + " • " + watchedAgo(entry.watchedAt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.MoreVert, "More", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun EpisodeWatchedGridCard(entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(4.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
                PosterImage(poster, Modifier.fillMaxSize())
                DurationBadge(entry.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp))
                Text("⋮", Modifier.align(Alignment.TopEnd).padding(3.dp), fontWeight = FontWeight.Bold)
            }
            Text(entry.title, Modifier.padding(horizontal = 3.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Episode ${entry.episode}", Modifier.padding(horizontal = 3.dp), fontSize = 9.sp)
            Text(entry.episodeTitle ?: "Judul episode belum tersedia", Modifier.padding(horizontal = 3.dp), fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(watchedAgo(entry.watchedAt), Modifier.padding(horizontal = 3.dp, vertical = 3.dp), fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EpisodeWatchedListCard(entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 118.dp, height = 76.dp)) {
                PosterImage(poster, Modifier.fillMaxSize())
                DurationBadge(entry.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Episode ${entry.episode}", fontSize = 12.sp)
                Text(entry.episodeTitle ?: "Judul episode belum tersedia", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(watchedAgo(entry.watchedAt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.MoreVert, "More", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PosterImage(url: String?, modifier: Modifier = Modifier) {
    Surface(modifier, RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        if (url.isNullOrBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("K", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        } else {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .12f))) {
        Box(Modifier.fillMaxWidth(progress).fillMaxSize().background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun DurationBadge(durationMs: Long, modifier: Modifier = Modifier) {
    Surface(modifier, RoundedCornerShape(5.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .78f)) {
        Text(if (durationMs > 0L) formatDuration(durationMs) else "—:—", Modifier.padding(horizontal = 5.dp, vertical = 2.dp), fontSize = 8.sp, color = MaterialTheme.colorScheme.onPrimary)
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private fun watchedAgo(timestamp: Long): String = if (timestamp <= 0L) "Riwayat lama" else DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

private fun animeWatchStatus(anime: Anime, episode: Int): AnimeWatchedFilter = when {
    anime.latestEpisode > 0 && episode >= anime.latestEpisode -> AnimeWatchedFilter.COMPLETED
    else -> AnimeWatchedFilter.IN_PROGRESS
}

private fun statusLabel(anime: Anime, episode: Int): String = when (animeWatchStatus(anime, episode)) {
    AnimeWatchedFilter.COMPLETED -> "Completed"
    AnimeWatchedFilter.IN_PROGRESS -> "In Progress"
    AnimeWatchedFilter.DROPPED -> "Dropped"
    AnimeWatchedFilter.ALL -> "Watched"
}

@Composable
private fun rememberScrollStateCompat() = androidx.compose.foundation.rememberScrollState()
