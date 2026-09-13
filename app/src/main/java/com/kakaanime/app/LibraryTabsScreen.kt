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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.kakaanime.app.data.KakaAnimePreferences

@Composable
fun LibraryTabsScreen(animeList: List<Anime>, onAnimeClick: (Anime) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { KakaAnimePreferences(context) }
    var selected by remember { mutableStateOf(0) }
    var refresh by remember { mutableStateOf(0) }
    var history by remember { mutableStateOf(prefs.loadWatchHistory()) }
    val favorites = prefs.loadFavoriteTitles()
    val favoriteAnime = animeList.filter { it.title in favorites }

    LaunchedEffect(refresh) { history = prefs.loadWatchHistory() }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LibraryTab("Favorite", selected == 0, Modifier.weight(1f)) { selected = 0 }
            LibraryTab("History", selected == 1, Modifier.weight(1f)) { selected = 1 }
        }
        if (selected == 0) {
            if (favoriteAnime.isEmpty()) EmptyLibrary("Belum ada anime favorit", "Anime yang kamu simpan akan muncul di sini.")
            else LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(favoriteAnime, key = { it.title }) { anime -> LibraryAnimeCard(anime, onAnimeClick) }
            }
        } else {
            HistoryLibraryContent(history, animeList, onAnimeClick) { refresh++ }
        }
    }
}

@Composable private fun LibraryTab(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(14.dp), color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
        Box(Modifier.fillMaxWidth().padding(vertical = 11.dp), contentAlignment = Alignment.Center) { Text(title, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun HistoryLibraryContent(history: List<com.kakaanime.app.data.WatchHistoryEntry>, animeList: List<Anime>, onAnimeClick: (Anime) -> Unit, onChanged: () -> Unit) {
    var clearConfirm by remember { mutableStateOf(false) }
    val continueItems = history.distinctBy { it.title }.take(3)
    Column(Modifier.fillMaxSize()) {
        if (history.isNotEmpty()) Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { clearConfirm = true }) { Text("Hapus semua") }
        }
        if (history.isEmpty()) EmptyLibrary("History masih kosong", "Anime yang kamu tonton akan muncul di sini.")
        else LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (continueItems.isNotEmpty()) item { Text("Continue Watching", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) }
            items(continueItems, key = { "continue-${it.title}" }) { entry ->
                val anime = animeList.firstOrNull { it.title == entry.title }
                if (anime != null) HistoryRow(entry, anime, onAnimeClick, onChanged)
            }
            item { Spacer(Modifier.height(6.dp)); Text("Recently Watched", style = MaterialTheme.typography.titleMedium) }
            items(history, key = { "history-${it.title}-${it.episode}-${it.watchedAt}" }) { entry ->
                val anime = animeList.firstOrNull { it.title == entry.title }
                if (anime != null) HistoryRow(entry, anime, onAnimeClick, onChanged)
            }
        }
    }
    if (clearConfirm) AlertDialog(onDismissRequest = { clearConfirm = false }, title = { Text("Hapus semua history?") }, text = { Text("Semua riwayat tontonan akan dihapus.") }, confirmButton = { Button(onClick = { KakaAnimePreferences(androidx.compose.ui.platform.LocalContext.current).clearWatchHistory(); clearConfirm = false; onChanged() }) { Text("Hapus") } }, dismissButton = { OutlinedButton(onClick = { clearConfirm = false }) { Text("Batal") } })
}

@Composable private fun HistoryRow(entry: com.kakaanime.app.data.WatchHistoryEntry, anime: Anime, onAnimeClick: (Anime) -> Unit, onChanged: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable { onAnimeClick(anime) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(anime.title, style = MaterialTheme.typography.titleMedium); Text("Episode ${entry.episode}", color = MaterialTheme.colorScheme.primary); entry.episodeTitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) } }
            IconButton(onClick = { confirm = true }) { Icon(Icons.Outlined.Delete, "Hapus dari history") }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Hapus dari history?") }, text = { Text("Episode ${entry.episode} dari ${anime.title} akan dihapus.") }, confirmButton = { Button(onClick = { KakaAnimePreferences(context).deleteWatchHistoryEntry(entry.title, entry.episode); confirm = false; onChanged() }) { Text("Hapus") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } })
}

@Composable private fun LibraryAnimeCard(anime: Anime, onClick: (Anime) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(anime) }) { Column(Modifier.padding(14.dp)) { Text(anime.title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text("${anime.type} · ${anime.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("★ ${anime.rating}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) } }
}

@Composable private fun EmptyLibrary(title: String, description: String) { Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
