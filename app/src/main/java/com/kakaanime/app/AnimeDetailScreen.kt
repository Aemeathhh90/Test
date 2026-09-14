package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.provider.ProviderEpisode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AnimeDetailTab { INFO, EPISODES }
private enum class EpisodeViewMode { LIST, GRID }

@Composable
fun AnimeDetailScreen(
    anime: Anime,
    isFavorite: Boolean,
    watchedEpisode: Int?,
    watchedEpisodes: Set<Int>,
    episodes: List<ProviderEpisode>,
    loading: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEpisodeClick: (Int) -> Unit,
    seasonOptions: List<Anime> = emptyList(),
    onSeasonSelected: (Anime) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(AnimeDetailTab.INFO) }
    var episodeViewMode by remember { mutableStateOf(EpisodeViewMode.LIST) }
    var episodeFilter by remember { mutableStateOf("Semua") }
    var episodeQuery by remember { mutableStateOf("") }
    var seasonMenuExpanded by remember { mutableStateOf(false) }

    val filteredEpisodes = remember(episodes, watchedEpisodes, episodeFilter, episodeQuery) {
        episodes.filter { ep ->
            val matchesFilter = when (episodeFilter) {
                "Belum Ditonton" -> ep.number !in watchedEpisodes
                "Terbaru" -> ep.isNew
                else -> true
            }
            val matchesQuery = episodeQuery.isBlank() ||
                ep.number.toString().contains(episodeQuery) ||
                ep.title.orEmpty().contains(episodeQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val hasSeasonSelector = seasonOptions.size > 1
    val selectedSeasonIndex = seasonOptions.indexOfFirst { it === anime }.takeIf { it >= 0 }
        ?: seasonOptions.indexOfFirst { it.title == anime.title && it.season == anime.season }.takeIf { it >= 0 }
        ?: 0
    val selectedSeason = seasonOptions.getOrNull(selectedSeasonIndex) ?: anime

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Persistent anime header: this area stays in place while INFO/EPISODES changes.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                text = anime.title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        // Compact persistent summary. The tab content below is the only part that switches.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .30f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .13f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(anime.title, fontWeight = FontWeight.Bold, maxLines = 2)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${anime.status.uppercase()} • ${anime.year} • ${anime.type}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(anime.rating, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        if (watchedEpisode != null) "Ep $watchedEpisode" else "Belum ditonton",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AnimeDetailTab.INFO -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    anime.genre,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("Tentang Anime", fontWeight = FontWeight.Bold)
                                Text(
                                    anime.description,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp,
                                    maxLines = 5
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DetailStatCard("Episode", episodes.size.toString(), Modifier.weight(1f))
                                DetailStatCard("Studio", anime.studio, Modifier.weight(1f))
                                DetailStatCard("Season", anime.season, Modifier.weight(1f))
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    onEpisodeClick(watchedEpisode ?: episodes.firstOrNull()?.number ?: 1)
                                },
                                enabled = episodes.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    when {
                                        episodes.isEmpty() -> "Episode Belum Tersedia"
                                        watchedEpisode != null -> "Lanjut Nonton"
                                        else -> "Mulai Nonton"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                AnimeDetailTab.EPISODES -> {
                    if (episodeViewMode == EpisodeViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                EpisodeControls(
                                    anime = anime,
                                    selectedSeason = selectedSeason,
                                    hasSeasonSelector = hasSeasonSelector,
                                    seasonMenuExpanded = seasonMenuExpanded,
                                    onSeasonMenuExpandedChange = { seasonMenuExpanded = it },
                                    seasonOptions = seasonOptions,
                                    selectedSeasonIndex = selectedSeasonIndex,
                                    onSeasonSelected = onSeasonSelected,
                                    episodeFilter = episodeFilter,
                                    onEpisodeFilterChange = { episodeFilter = it },
                                    episodeQuery = episodeQuery,
                                    onEpisodeQueryChange = { episodeQuery = it },
                                    viewMode = episodeViewMode,
                                    onViewModeChange = { episodeViewMode = it }
                                )
                            }
                            if (loading) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    DetailLoadingState()
                                }
                            } else if (episodes.isEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    DetailStateCard("Episode belum tersedia", "Anime ini belum memiliki episode yang bisa ditonton.")
                                }
                            } else if (filteredEpisodes.isEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    DetailStateCard("Tidak ada hasil", "Coba filter atau nomor episode lain.")
                                }
                            } else {
                                items(filteredEpisodes, key = { it.number }) { ep ->
                                    EpisodeGridCard(ep, ep.number in watchedEpisodes, { onEpisodeClick(ep.number) })
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            item {
                                EpisodeControls(
                                    anime = anime,
                                    selectedSeason = selectedSeason,
                                    hasSeasonSelector = hasSeasonSelector,
                                    seasonMenuExpanded = seasonMenuExpanded,
                                    onSeasonMenuExpandedChange = { seasonMenuExpanded = it },
                                    seasonOptions = seasonOptions,
                                    selectedSeasonIndex = selectedSeasonIndex,
                                    onSeasonSelected = onSeasonSelected,
                                    episodeFilter = episodeFilter,
                                    onEpisodeFilterChange = { episodeFilter = it },
                                    episodeQuery = episodeQuery,
                                    onEpisodeQueryChange = { episodeQuery = it },
                                    viewMode = episodeViewMode,
                                    onViewModeChange = { episodeViewMode = it }
                                )
                            }
                            if (loading) {
                                item { DetailLoadingState() }
                            } else if (episodes.isEmpty()) {
                                item { DetailStateCard("Episode belum tersedia", "Anime ini belum memiliki episode yang bisa ditonton.") }
                            } else if (filteredEpisodes.isEmpty()) {
                                item { DetailStateCard("Tidak ada hasil", "Coba filter atau nomor episode lain.") }
                            } else {
                                items(filteredEpisodes, key = { it.number }) { ep ->
                                    EpisodeListCard(ep, ep.number in watchedEpisodes) { onEpisodeClick(ep.number) }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detail navigation is intentionally different from the global 5-tab navigation.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 34.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DetailTabItem(
                selected = selectedTab == AnimeDetailTab.INFO,
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                label = "INFO",
                onClick = { selectedTab = AnimeDetailTab.INFO },
                modifier = Modifier.weight(1f)
            )
            DetailTabItem(
                selected = selectedTab == AnimeDetailTab.EPISODES,
                icon = { Icon(Icons.Outlined.Movie, contentDescription = null) },
                label = "EPISODES",
                onClick = { selectedTab = AnimeDetailTab.EPISODES },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EpisodeControls(
    anime: Anime,
    selectedSeason: Anime,
    hasSeasonSelector: Boolean,
    seasonMenuExpanded: Boolean,
    onSeasonMenuExpandedChange: (Boolean) -> Unit,
    seasonOptions: List<Anime>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Anime) -> Unit,
    episodeFilter: String,
    onEpisodeFilterChange: (String) -> Unit,
    episodeQuery: String,
    onEpisodeQueryChange: (String) -> Unit,
    viewMode: EpisodeViewMode,
    onViewModeChange: (EpisodeViewMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        if (hasSeasonSelector) {
            Box {
                OutlinedButton(
                    onClick = { onSeasonMenuExpandedChange(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        selectedSeason.season.ifBlank { "Season ${selectedSeasonIndex + 1}" },
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("▼", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = seasonMenuExpanded,
                    onDismissRequest = { onSeasonMenuExpandedChange(false) }
                ) {
                    seasonOptions.forEachIndexed { index, season ->
                        DropdownMenuItem(
                            text = { Text(season.season.ifBlank { "Season ${index + 1}" }) },
                            onClick = {
                                onSeasonMenuExpandedChange(false)
                                onSeasonSelected(season)
                            }
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Episodes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { onViewModeChange(EpisodeViewMode.LIST) }) {
                Icon(Icons.Filled.List, contentDescription = "Tampilan list", tint = if (viewMode == EpisodeViewMode.LIST) MaterialTheme.colorScheme.primary else LocalContentColor.current)
            }
            IconButton(onClick = { onViewModeChange(EpisodeViewMode.GRID) }) {
                Icon(Icons.Filled.GridView, contentDescription = "Tampilan grid", tint = if (viewMode == EpisodeViewMode.GRID) MaterialTheme.colorScheme.primary else LocalContentColor.current)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Semua", "Terbaru", "Belum Ditonton").forEach { filter ->
                FilterChip(
                    selected = episodeFilter == filter,
                    onClick = { onEpisodeFilterChange(filter) },
                    label = { Text(filter, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = episodeQuery,
            onValueChange = { onEpisodeQueryChange(it.filter(Char::isDigit).take(5)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            placeholder = { Text("Cari episode...") }
        )
    }
}

@Composable
private fun DetailTabItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(4.dp).fillMaxWidth()) {
            if (selected) {
                Box(
                    Modifier
                        .fillMaxWidth(.72f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalContentColor provides if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) {
                icon()
            }
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .32f)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2)
        }
    }
}

@Composable
private fun EpisodeListCard(episode: ProviderEpisode, watched: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (watched) .30f else .46f)
    ) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(96.dp).height(62.dp).clip(RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title ?: "Episode ${episode.number}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = if (watched) .16f else .50f)))
                Icon(
                    if (watched) Icons.Filled.PlayArrow else Icons.Outlined.Lock,
                    contentDescription = if (watched) "Sudah ditonton" else "Episode terkunci",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Episode ${episode.number}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(episode.title ?: "Episode ${episode.number}", fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(formatReleaseDate(episode.releasedAt), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (episode.isNew) {
                Text("NEW", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EpisodeGridCard(episode: ProviderEpisode, watched: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1.55f).clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title ?: "Episode ${episode.number}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = if (watched) .15f else .48f)))
                Icon(
                    if (watched) Icons.Filled.PlayArrow else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(Modifier.padding(10.dp)) {
                Text("Episode ${episode.number}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(episode.title ?: "Episode ${episode.number}", fontWeight = FontWeight.SemiBold, maxLines = 2)
                if (episode.isNew) Text("NEW", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailLoadingState() {
    Box(Modifier.fillMaxWidth().padding(34.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DetailStateCard(title: String, message: String) {
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatReleaseDate(timestamp: Long?): String = timestamp?.let {
    runCatching { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) }
        .getOrDefault("Tanggal tidak tersedia")
} ?: "Tanggal tidak tersedia"
