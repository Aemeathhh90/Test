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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Kembali") }
                Text("Detail Anime", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
        item {
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(anime.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${anime.rating}", fontWeight = FontWeight.SemiBold)
                        Text("  •  ${anime.year}  •  ${anime.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(anime.genre, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(anime.description, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    Text(
                        if (watchedEpisode != null) "Lanjut dari Episode $watchedEpisode" else "Belum mulai menonton",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium
                    )
                    Button(
                        onClick = { onEpisodeClick(watchedEpisode ?: episodes.firstOrNull()?.number ?: 1) },
                        enabled = episodes.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (episodes.isEmpty()) "Episode Belum Tersedia"
                            else if (watchedEpisode != null) "Lanjut Nonton"
                            else "Mulai Nonton",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        if (hasSeasonSelector) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Season", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.fillMaxWidth(),
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
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = if (hasSeasonSelector) 0.dp else 22.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Episode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${episodes.size} Episode", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Terbaru", "Belum Ditonton").forEach { filter ->
                    FilterChip(selected = episodeFilter == filter, onClick = { episodeFilter = filter }, label = { Text(filter, fontSize = 12.sp) })
                }
            }
        }
        item {
            OutlinedTextField(
                value = episodeQuery,
                onValueChange = { episodeQuery = it.filter(Char::isDigit).take(5) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text("Cari nomor episode...") }
            )
        }
        if (loading) {
            item { Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else if (episodes.isEmpty()) {
            item { DetailStateCard("Episode belum tersedia", "Provider belum mengirim daftar episode untuk anime ini.") }
        } else if (filteredEpisodes.isEmpty()) {
            item { DetailStateCard("Tidak ada hasil", "Coba filter atau nomor episode lain.") }
        } else {
            items(filteredEpisodes, key = { it.number }) { ep ->
                val watched = ep.number in watchedEpisodes
                EpisodeListCard(
                    episode = ep,
                    watched = watched,
                    onClick = { onEpisodeClick(ep.number) }
                )
            }
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
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (watched) .30f else .48f)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.width(104.dp).height(68.dp).clip(RoundedCornerShape(12.dp)),
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
                Text("Episode ${episode.number}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(episode.title ?: "Episode ${episode.number}", fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(
                    formatReleaseDate(episode.releasedAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (episode.isNew) {
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
                    Text("NEW", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatReleaseDate(timestamp: Long?): String = timestamp?.let {
    runCatching { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) }.getOrDefault("Tanggal tidak tersedia")
} ?: "Tanggal tidak tersedia"

@Composable
private fun DetailStateCard(title: String, message: String) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
