package com.kakaanime.app

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kakaanime.app.data.AnimeStateIdentity
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.SeasonAwareStateRepository
import com.kakaanime.app.data.SeasonAwareWatchHistoryEntry

private data class LibraryHistoryItem(
    val identity: AnimeStateIdentity,
    val title: String,
    val episode: Int,
    val episodeTitle: String?,
    val watchedAt: Long,
)

@Composable
fun LibraryTabsScreen(animeList: List<Anime>, onAnimeClick: (Anime) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { KakaAnimePreferences(context) }
    val stateRepository = remember(prefs) { SeasonAwareStateRepository(prefs) }
    var selected by remember { mutableStateOf(0) }
    var refresh by remember { mutableStateOf(0) }
    var history by remember { mutableStateOf(emptyList<LibraryHistoryItem>()) }

    val favoriteGroupIds = prefs.loadFavoriteGroupIds()
    val legacyFavoriteTitles = prefs.loadFavoriteTitles()
    val favoriteGroupIdsWithLegacyFallback = remember(animeList, favoriteGroupIds, legacyFavoriteTitles) {
        favoriteGroupIds + animeList
            .filter { it.title in legacyFavoriteTitles }
            .map { it.animeGroupId.ifBlank { it.title } }
            .toSet()
    }
    val favoriteAnime = animeList
        .filter { (it.animeGroupId.ifBlank { it.title }) in favoriteGroupIdsWithLegacyFallback }
        .distinctBy { it.animeGroupId.ifBlank { it.title } }

    fun identityFor(anime: Anime) = stateRepository.identity(
        animeGroupId = anime.animeGroupId.ifBlank { anime.title },
        seasonNumber = anime.seasonNumber,
        seasonTitle = anime.seasonTitle,
    )

    fun buildHistory(): List<LibraryHistoryItem> {
        val seasonAware = stateRepository.history().map { entry ->
            LibraryHistoryItem(
                identity = entry.identity,
                title = entry.title ?: animeList.firstOrNull { it.animeGroupId == entry.animeGroupId }?.title ?: entry.animeGroupId,
                episode = entry.episode,
                episodeTitle = entry.episodeTitle,
                watchedAt = entry.watchedAt,
            )
        }
        val seasonAwareKeys = seasonAware.map { "${it.identity.episodeKey(it.episode)}::${it.watchedAt}" }.toSet()
        val legacy = prefs.loadWatchHistory().mapNotNull { entry ->
            val matches = animeList.filter { it.title == entry.title }
            if (matches.isEmpty()) return@mapNotNull null
            val groups = matches.map { it.animeGroupId.ifBlank { it.title } }.distinct()
            if (groups.size != 1) return@mapNotNull null
            val anime = matches.first()
            val item = LibraryHistoryItem(identityFor(anime), entry.title, entry.episode, entry.episodeTitle, entry.watchedAt)
            if (seasonAwareKeys.contains("${item.identity.episodeKey(item.episode)}::${item.watchedAt}")) null else item
        }
        return (seasonAware + legacy).sortedByDescending { it.watchedAt }
    }

    LaunchedEffect(refresh, animeList) { history = buildHistory() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LibraryHeader(selected = selected) { selected = it }
        if (selected == 0) {
            if (favoriteAnime.isEmpty()) {
                EmptyLibrary(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "Belum ada anime favorit",
                    description = "Anime yang kamu simpan akan muncul di sini."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteAnime, key = { it.animeGroupId.ifBlank { it.title } }) { anime ->
                        LibraryAnimeCard(anime, onAnimeClick)
                    }
                }
            }
        } else {
            HistoryLibraryContent(history, animeList, onAnimeClick) { refresh++ }
        }
    }
}

@Composable
private fun LibraryHeader(selected: Int, onSelected: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text("Library", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            if (selected == 0) "Koleksi anime yang kamu simpan" else "Lanjutkan dan lihat riwayat tontonanmu",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LibraryTab("Favorite", selected == 0, Modifier.weight(1f)) { onSelected(0) }
            LibraryTab("History", selected == 1, Modifier.weight(1f)) { onSelected(1) }
        }
    }
}

@Composable
private fun LibraryTab(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = .12f),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoryLibraryContent(
    history: List<LibraryHistoryItem>,
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onChanged: () -> Unit
) {
    var clearConfirm by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val stateRepository = remember { SeasonAwareStateRepository(KakaAnimePreferences(context)) }
    val continueItems = history.distinctBy { it.identity.episodeKey(it.episode).substringBefore("::episode:") }.take(3)

    fun animeFor(item: LibraryHistoryItem): Anime? = animeList.firstOrNull {
        (it.animeGroupId.ifBlank { it.title }) == item.identity.animeGroupId &&
            it.seasonNumber == item.identity.seasonNumber &&
            it.seasonTitle == item.identity.seasonTitle
    } ?: animeList.firstOrNull { it.title == item.title }

    Column(Modifier.fillMaxSize()) {
        if (history.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Your History", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${history.size} riwayat tontonan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { clearConfirm = true }) { Text("Hapus semua") }
            }
        }

        if (history.isEmpty()) {
            EmptyLibrary(
                icon = Icons.Outlined.History,
                title = "History masih kosong",
                description = "Anime yang kamu tonton akan muncul di sini."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (continueItems.isNotEmpty()) item { SectionTitle("Continue Watching", "Lanjutkan dari episode terakhir") }
                items(continueItems, key = { "continue-${it.identity.episodeKey(it.episode)}" }) { entry ->
                    val anime = animeFor(entry)
                    if (anime != null) HistoryRow(entry, anime, onAnimeClick, onChanged)
                }
                item { SectionTitle("Recently Watched", "Riwayat tontonan terbaru", topPadding = 8.dp) }
                items(history, key = { "history-${it.identity.episodeKey(it.episode)}-${it.watchedAt}" }) { entry ->
                    val anime = animeFor(entry)
                    if (anime != null) HistoryRow(entry, anime, onAnimeClick, onChanged)
                }
            }
        }
    }

    if (clearConfirm) {
        AlertDialog(
            onDismissRequest = { clearConfirm = false },
            title = { Text("Hapus semua history?") },
            text = { Text("Semua riwayat tontonan akan dihapus.") },
            confirmButton = {
                Button(onClick = {
                    stateRepository.clearHistory()
                    clearConfirm = false
                    onChanged()
                }) { Text("Hapus") }
            },
            dismissButton = { OutlinedButton(onClick = { clearConfirm = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, topPadding: Int = 0) {
    Column(Modifier.fillMaxWidth().padding(top = topPadding.dp, bottom = 2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryRow(
    entry: LibraryHistoryItem,
    anime: Anime,
    onAnimeClick: (Anime) -> Unit,
    onChanged: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val stateRepository = remember(context) { SeasonAwareStateRepository(KakaAnimePreferences(context)) }
    var confirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAnimeClick(anime) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp, 46.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(anime.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                val seasonLabel = entry.identity.seasonNumber?.let { "Season $it" } ?: entry.identity.seasonTitle
                if (!seasonLabel.isNullOrBlank()) Text(seasonLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Episode ${entry.episode}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                entry.episodeTitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
            }
            IconButton(onClick = { confirm = true }) { Icon(Icons.Outlined.DeleteOutline, "Hapus dari history") }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Hapus dari history?") },
            text = { Text("Episode ${entry.episode} dari ${anime.title} akan dihapus.") },
            confirmButton = {
                Button(onClick = {
                    stateRepository.deleteHistory(entry.identity, entry.episode)
                    confirm = false
                    onChanged()
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun LibraryAnimeCard(anime: Anime, onClick: (Anime) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(anime) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(anime.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            val seasonLabel = anime.seasonNumber?.let { "Season $it" } ?: anime.seasonTitle
            if (!seasonLabel.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(seasonLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(6.dp))
            Text("${anime.type} · ${anime.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text("★ ${anime.rating}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EmptyLibrary(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(16.dp).size(30.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
