package com.kakaanime.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Search
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
import com.kakaanime.app.ui.home.HomeAnimeUi

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onAnimeClick: (HomeAnimeUi) -> Unit = {},
    onFavoriteToggle: (HomeAnimeUi) -> Unit = {},
) {
    var query by remember(state.searchQuery) { mutableStateOf(state.searchQuery) }
    val favorites = remember(state.favorites, query) {
        state.favorites.filter { query.isBlank() || it.title.contains(query, true) || it.genre.contains(query, true) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 116.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Library", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Anime favorit kamu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.Favorite, "Favorite", tint = MaterialTheme.colorScheme.primary)
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, "Cari favorite") },
                    placeholder = { Text("Cari di favorite...") },
                )
            }
        }
        if (favorites.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Surface(
                    Modifier.fillMaxWidth().padding(top = 20.dp),
                    RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f),
                ) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(if (query.isBlank()) "Belum ada anime favorit" else "Favorite tidak ditemukan", fontWeight = FontWeight.Bold)
                        Text(if (query.isBlank()) "Tekan ikon hati di halaman detail untuk menambahkan anime." else "Coba kata kunci lain.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(favorites, key = { it.id }) { anime ->
                Column(Modifier.fillMaxWidth().clickable { onAnimeClick(anime) }) {
                    Box(Modifier.fillMaxWidth().aspectRatio(.68f).clip(RoundedCornerShape(16.dp))) {
                        if (anime.posterUrl != null) AsyncImage(anime.posterUrl, anime.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text("POSTER", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                        IconButton(onClick = { onFavoriteToggle(anime) }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.scrim.copy(alpha = .62f)) {
                                Icon(Icons.Outlined.Favorite, "Hapus dari Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("Ep ${anime.latestEpisode} • ★ ${anime.rating}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}
