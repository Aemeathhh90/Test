package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
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

private enum class AnimeDetailTab { INFO, WATCH }

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
    var episodeFilter by remember { mutableStateOf("Semua") }
    var episodeQuery by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                fontSize = 18.sp
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AnimeDetailTab.INFO -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)
                                        ) {
                                            Text(
                                                text = anime.status.uppercase(),
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "${anime.type} • ${anime.year}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        anime.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(anime.rating, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "  •  ${anime.studio}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        anime.genre,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .28f)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Tentang Anime", fontWeight = FontWeight.Bold)
                                    Text(
                                        anime.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp,
                                        maxLines = 5
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DetailStatCard("Episode", episodes.size.toString(), Modifier.weight(1f))
                                DetailStatCard("Terakhir", watchedEpisode?.let { "Ep $it" } ?: "Belum", Modifier.weight(1f))
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    onEpisodeClick(watchedEpisode ?: episodes.firstOrNull()?.number ?: 1)
                                },
                                enabled = episodes.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp)
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

                AnimeDetailTab.WATCH -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Episodes",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${episodes.size} Episode",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (hasSeasonSelector) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        seasonOptions.forEachIndexed { index, season ->
                                            FilterChip(
                                                selected = index == selectedSeasonIndex,
                                                onClick = { onSeasonSelected(season) },
                                                label = {
                                                    Text(
                                                        season.season.ifBlank { "Season ${index + 1}" },
                                                        maxLines = 1
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Semua", "Terbaru", "Belum Ditonton").forEach { filter ->
                                        FilterChip(
                                            selected = episodeFilter == filter,
                                            onClick = { episodeFilter = filter },
                                            label = { Text(filter, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = episodeQuery,
                                    onValueChange = { episodeQuery = it.filter(Char::isDigit).take(5) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                                    placeholder = { Text("Cari nomor episode...") }
                                )
                            }
                        }

                        if (loading) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (episodes.isEmpty()) {
                            item {
                                DetailStateCard(
                                    "Episode belum tersedia",
                                    "Provider belum mengirim daftar episode untuk anime ini."
                                )
                            }
                        } else if (filteredEpisodes.isEmpty()) {
                            item {
                                DetailStateCard(
                                    "Tidak ada hasil",
                                    "Coba filter atau nomor episode lain."
                                )
                            }
                        } else {
                            items(filteredEpisodes, key = { it.number }) { ep ->
                                EpisodeListCard(
                                    episode = ep,
                                    watched = ep.number in watchedEpisodes,
                                    onClick = { onEpisodeClick(ep.number) }
                                )
                            }
                        }
                    }
                }
            }
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .98f)
        ) {
            NavigationBarItem(
                selected = selectedTab == AnimeDetailTab.INFO,
                onClick = { selectedTab = AnimeDetailTab.INFO },
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                label = { Text("INFO") }
            )
            NavigationBarItem(
                selected = selectedTab == AnimeDetailTab.WATCH,
                onClick = { selectedTab = AnimeDetailTab.WATCH },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                label = { Text("WATCH") }
            )
        }
    }
}

@Composable
private fun DetailStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .32f)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EpisodeListCard(
    episode: ProviderEpisode,
    watched: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (watched) .30f else .48f)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(104.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title ?: "Episode ${episode.number}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize().background(
                        MaterialTheme.colorScheme.scrim.copy(alpha = if (watched) .18f else .52f)
                    )
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = .72f)
                ) {
                    Icon(
                        if (watched) Icons.Filled.PlayArrow else Icons.Outlined.Lock,
                        contentDescription = if (watched) "Sudah ditonton" else "Episode terkunci",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(6.dp).size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Episode ${episode.number}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    episode.title ?: "Episode ${episode.number}",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Text(
                    formatReleaseDate(episode.releasedAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (episode.isNew) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)
                ) {
                    Text(
                        "NEW",
                        Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatReleaseDate(timestamp: Long?): String = timestamp?.let {
    runCatching {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    }.getOrDefault("Tanggal tidak tersedia")
} ?: "Tanggal tidak tersedia"

@Composable
private fun DetailStateCard(title: String, message: String) {
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
