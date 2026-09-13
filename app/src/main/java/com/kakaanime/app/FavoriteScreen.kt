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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.data.AniListMetadataService

@Composable
fun FavoriteScreen(
    animeList: List<Anime>,
    watchedEpisodes: Map<String, Int>,
    onAnimeClick: (Anime) -> Unit
) {
    val metadataService = remember { AniListMetadataService() }
    var search by remember { mutableStateOf("") }
    var gridMode by remember { mutableStateOf(false) }
    var posterUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(animeList) {
        posterUrls = buildMap {
            animeList.forEach { anime ->
                metadataService.findByTitle(anime.title)?.imageUrl?.let { put(anime.title, it) }
            }
        }
    }

    val filtered = animeList.filter { it.title.contains(search, ignoreCase = true) }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        FavoriteHeader(
            search = search,
            onSearchChanged = { search = it },
            gridMode = gridMode,
            onToggleGrid = { gridMode = !gridMode }
        )

        if (filtered.isEmpty()) {
            FavoriteEmptyState(search.isNotBlank())
        } else if (gridMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filtered, key = { it.title }) { anime ->
                    FavoriteGridCard(anime, posterUrls[anime.title], watchedEpisodes[anime.title], onAnimeClick)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.title }) { anime ->
                    FavoriteListCard(anime, posterUrls[anime.title], watchedEpisodes[anime.title], onAnimeClick)
                }
            }
        }
    }
}

@Composable
private fun FavoriteHeader(
    search: String,
    onSearchChanged: (String) -> Unit,
    gridMode: Boolean,
    onToggleGrid: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Favorite", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Anime yang kamu simpan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)) {
                IconButton(onClick = onToggleGrid) {
                    Icon(
                        if (gridMode) Icons.Outlined.List else Icons.Outlined.GridView,
                        contentDescription = if (gridMode) "Tampilan list" else "Tampilan grid"
                    )
                }
            }
        }

        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(9.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = search,
                    onValueChange = onSearchChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { inner ->
                        if (search.isEmpty()) Text("Cari anime favorit...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteListCard(
    anime: Anime,
    posterUrl: String?,
    watchedEpisode: Int?,
    onClick: (Anime) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))
            .clickable { onClick(anime) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FavoritePoster(posterUrl, anime.title, Modifier.size(64.dp, 88.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(anime.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Text("${anime.type} · ${anime.year} · ${anime.status}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(anime.genre, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(
                if (watchedEpisode != null) "Ep $watchedEpisode / ${anime.latestEpisode}  ·  ★ ${anime.rating}" else "Ep ${anime.latestEpisode}  ·  ★ ${anime.rating}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (watchedEpisode != null) {
                Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxWidth((watchedEpisode.toFloat() / anime.latestEpisode.coerceAtLeast(1)).coerceIn(0f, 1f)).fillMaxSize().background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
private fun FavoriteGridCard(
    anime: Anime,
    posterUrl: String?,
    watchedEpisode: Int?,
    onClick: (Anime) -> Unit
) {
    Column(Modifier.fillMaxWidth().clickable { onClick(anime) }) {
        Box(
            Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            FavoritePoster(posterUrl, anime.title, Modifier.fillMaxSize())
            if (watchedEpisode != null) {
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(
            if (watchedEpisode != null) "Ep $watchedEpisode / ${anime.latestEpisode} · ★ ${anime.rating}" else "Ep ${anime.latestEpisode} · ★ ${anime.rating}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun FavoritePoster(url: String?, title: String, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        if (url != null) {
            AsyncImage(url, title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text(title.take(2).uppercase(), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FavoriteEmptyState(searching: Boolean) {
    Box(Modifier.fillMaxSize().padding(bottom = 72.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (searching) "Anime tidak ditemukan" else "Belum ada anime favorit",
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (searching) "Coba kata kunci lain." else "Anime yang kamu pilih akan muncul di sini.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
