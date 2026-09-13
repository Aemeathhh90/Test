package com.kakaanime.app

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
private enum class AnimeFilter { ALL, IN_PROGRESS, COMPLETED, DROPPED }
private enum class EpisodeFilter { ALL, TODAY, WEEK, MONTH }

@Composable
fun AnimeWatchedScreen(preferences: KakaAnimePreferences, animeList: List<Anime>, onBack: () -> Unit, onAnimeClick: (Anime) -> Unit) {
    var layout by remember { mutableStateOf(WatchLayout.GRID) }
    var filter by remember { mutableStateOf(AnimeFilter.ALL) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Last Watched") }
    var sortOpen by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf(emptyMap<String, String>()) }
    val legacy = preferences.loadWatchedEpisodes()
    val history = preferences.loadWatchHistory()
    val records = history.groupBy { it.title }.values.mapNotNull { entries ->
        val entry = entries.maxByOrNull { it.watchedAt } ?: return@mapNotNull null
        animeList.firstOrNull { it.title.equals(entry.title, true) }?.let { it to entry }
    }.ifEmpty { legacy.mapNotNull { (title, ep) -> animeList.firstOrNull { it.title.equals(title, true) }?.let { it to WatchHistoryEntry(title, ep, null, 0L, 0L) } } }
    val shown = records.filter { (anime, entry) ->
        val status = animeStatus(anime, entry.episode)
        (filter == AnimeFilter.ALL || status == filter) && (query.isBlank() || anime.title.contains(query, true) || anime.genre.contains(query, true))
    }.let { if (sort == "Last Watched") it.sortedByDescending { it.second.watchedAt } else it.sortedBy { it.first.title.lowercase() } }
    LaunchedEffect(records.map { it.first.title }) { posters = AniListPosterService().getPosterUrls(records.map { it.first.title }) }

    WatchShell("Anime Watched", onBack, query, { query = it }, layout, { layout = it }, sort, { sortOpen = true }, sortOpen, { sortOpen = false }, { sort = it; sortOpen = false }, listOf("Last Watched", "Title"), shown.size, {
        listOf(AnimeFilter.ALL to "All", AnimeFilter.IN_PROGRESS to "In Progress", AnimeFilter.COMPLETED to "Completed", AnimeFilter.DROPPED to "Dropped").forEach { (value, label) -> FilterChip(filter == value, { filter = value }, label = { Text(label, fontSize = 11.sp) }) }
    }) {
        if (layout == WatchLayout.GRID) LazyVerticalGrid(GridCells.Fixed(4), Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown, key = { it.first.title }) { (anime, entry) -> AnimeGridCard(anime, entry, posters[anime.title]) { onAnimeClick(anime) } }
        } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown, key = { it.first.title }) { (anime, entry) -> AnimeListCard(anime, entry, posters[anime.title]) { onAnimeClick(anime) } }
        }
    }
}

@Composable
fun EpisodeWatchedScreen(preferences: KakaAnimePreferences, animeList: List<Anime>, onBack: () -> Unit, onEpisodeClick: (Anime, Int) -> Unit) {
    var layout by remember { mutableStateOf(WatchLayout.GRID) }
    var filter by remember { mutableStateOf(EpisodeFilter.ALL) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Newest") }
    var sortOpen by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf(emptyMap<String, String>()) }
    val history = preferences.loadWatchHistory()
    val now = System.currentTimeMillis()
    val shown = history.filter { entry ->
        val age = if (entry.watchedAt > 0L) now - entry.watchedAt else Long.MAX_VALUE
        val timeOk = when (filter) { EpisodeFilter.ALL -> true; EpisodeFilter.TODAY -> age <= 86_400_000L; EpisodeFilter.WEEK -> age <= 604_800_000L; EpisodeFilter.MONTH -> age <= 2_678_400_000L }
        timeOk && (query.isBlank() || entry.title.contains(query, true) || entry.episodeTitle?.contains(query, true) == true)
    }.let { if (sort == "Newest") it.sortedByDescending { it.watchedAt } else it.sortedBy { it.title.lowercase() } }
    LaunchedEffect(shown.map { it.title }) { posters = AniListPosterService().getPosterUrls(shown.map { it.title }) }

    WatchShell("Episode Watched", onBack, query, { query = it }, layout, { layout = it }, sort, { sortOpen = true }, sortOpen, { sortOpen = false }, { sort = it; sortOpen = false }, listOf("Newest", "Title"), shown.size, {
        listOf(EpisodeFilter.ALL to "All", EpisodeFilter.TODAY to "Today", EpisodeFilter.WEEK to "This Week", EpisodeFilter.MONTH to "This Month").forEach { (value, label) -> FilterChip(filter == value, { filter = value }, label = { Text(label, fontSize = 11.sp) }) }
    }) {
        if (layout == WatchLayout.GRID) LazyVerticalGrid(GridCells.Fixed(4), Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown, key = { "${it.title}-${it.episode}" }) { entry -> EpisodeGridCard(entry, posters[entry.title]) { animeList.firstOrNull { it.title.equals(entry.title, true) }?.let { onEpisodeClick(it, entry.episode) } } }
        } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown, key = { "${it.title}-${it.episode}" }) { entry -> EpisodeListCard(entry, posters[entry.title]) { animeList.firstOrNull { it.title.equals(entry.title, true) }?.let { onEpisodeClick(it, entry.episode) } } }
        }
    }
}

@Composable
private fun WatchShell(title: String, onBack: () -> Unit, query: String, onQuery: (String) -> Unit, layout: WatchLayout, onLayout: (WatchLayout) -> Unit, sort: String, onSort: () -> Unit, sortOpen: Boolean, onDismissSort: () -> Unit, onSelectSort: (String) -> Unit, sortValues: List<String>, total: Int, filters: @Composable RowScope.() -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text(title, 22.sp, FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, "More") }
        }
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(52.dp), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search", fontSize = 12.sp) })
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp), content = filters)
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Sort, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(6.dp))
            Text("Sort: $sort", 12.sp, modifier = Modifier.clickable(onClick = onSort)); Spacer(Modifier.weight(1f)); Text("Total: $total", 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = { onLayout(if (layout == WatchLayout.GRID) WatchLayout.LIST else WatchLayout.GRID) }) { Icon(if (layout == WatchLayout.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView, "Toggle layout") }
            DropdownMenu(sortOpen, onDismissRequest = onDismissSort) { sortValues.forEach { value -> DropdownMenuItem(text = { Text(if (value == sort) "✓ $value" else value) }, onClick = { onSelectSort(value) }) } }
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun AnimeGridCard(anime: Anime, entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    val progress = if (anime.latestEpisode > 0) (entry.episode.toFloat() / anime.latestEpisode).coerceIn(0f, 1f) else 0f
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(4.dp)) { Poster(poster, Modifier.fillMaxWidth().aspectRatio(2f / 3f)); Text(anime.title, Modifier.padding(3.dp), 10.sp, FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${entry.episode} / ${anime.latestEpisode}", Modifier.padding(horizontal = 3.dp), 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Progress(progress) }
    }
}

@Composable
private fun AnimeListCard(anime: Anime, entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    val progress = if (anime.latestEpisode > 0) (entry.episode.toFloat() / anime.latestEpisode).coerceIn(0f, 1f) else 0f
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) { Poster(poster, Modifier.size(82.dp, 112.dp)); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(anime.title, 15.sp, FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(anime.genre, 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(6.dp)); Text("Episode ${entry.episode} / ${anime.latestEpisode}", 12.sp); Progress(progress); Text("${animeStatusLabel(anime, entry.episode)} • ${watchedAgo(entry.watchedAt)}", 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Outlined.MoreVert, "More", Modifier.size(20.dp)) }
    }
}

@Composable
private fun EpisodeGridCard(entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f)) {
        Column(Modifier.padding(4.dp)) { Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) { Poster(poster, Modifier.fillMaxSize()); DurationBadge(entry.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp)); Icon(Icons.Outlined.MoreVert, "More", Modifier.align(Alignment.TopEnd).size(17.dp)) }; Text(entry.title, Modifier.padding(3.dp), 10.sp, FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("Episode ${entry.episode}", Modifier.padding(horizontal = 3.dp), 9.sp); Text(entry.episodeTitle ?: "Judul episode belum tersedia", Modifier.padding(horizontal = 3.dp), 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(watchedAgo(entry.watchedAt), Modifier.padding(3.dp), 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EpisodeListCard(entry: WatchHistoryEntry, poster: String?, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(118.dp, 76.dp)) { Poster(poster, Modifier.fillMaxSize()); DurationBadge(entry.durationMs, Modifier.align(Alignment.BottomEnd).padding(5.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(entry.title, 14.sp, FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("Episode ${entry.episode}", 12.sp); Text(entry.episodeTitle ?: "Judul episode belum tersedia", 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(watchedAgo(entry.watchedAt), 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Outlined.MoreVert, "More", Modifier.size(20.dp)) }
    }
}

@Composable
private fun Poster(url: String?, modifier: Modifier) {
    Surface(modifier, RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) { if (url.isNullOrBlank()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("K", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } else AsyncImage(url, null, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop) }
}

@Composable
private fun Progress(value: Float) { Box(Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 4.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .12f))) { Box(Modifier.fillMaxWidth(value).fillMaxHeight().background(MaterialTheme.colorScheme.primary)) } }

@Composable
private fun DurationBadge(durationMs: Long, modifier: Modifier) { Surface(modifier, RoundedCornerShape(5.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .8f)) { Text(if (durationMs > 0) "%d:%02d".format(durationMs / 60000, (durationMs / 1000) % 60) else "—:—", Modifier.padding(horizontal = 5.dp, vertical = 2.dp), 8.sp, color = MaterialTheme.colorScheme.onPrimary) } }

private fun watchedAgo(timestamp: Long): String = if (timestamp <= 0L) "Riwayat lama" else DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
private fun animeStatus(anime: Anime, episode: Int): AnimeFilter = if (anime.latestEpisode > 0 && episode >= anime.latestEpisode) AnimeFilter.COMPLETED else AnimeFilter.IN_PROGRESS
private fun animeStatusLabel(anime: Anime, episode: Int): String = if (animeStatus(anime, episode) == AnimeFilter.COMPLETED) "Completed" else "In Progress"
