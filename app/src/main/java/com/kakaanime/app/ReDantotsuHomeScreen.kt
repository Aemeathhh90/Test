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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.network.AnimeRepository

@Composable
fun ReDantotsuHomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var backendAnime by remember(animeList) { mutableStateOf(animeList) }

    LaunchedEffect(animeList) {
        backendAnime = AnimeRepository.loadAnime(animeList)
    }

    val filtered = backendAnime.filter { it.title.contains(search, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item { HomeReferenceHeader(search = search, onSearchChanged = { search = it }) }

        if (search.isNotBlank()) {
            item { Text("Hasil Pencarian", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            if (filtered.isEmpty()) item { EmptyHomeState() }
            else items(filtered) { anime -> SearchAnimeRow(anime, onAnimeClick) }
        } else {
            item { FeaturedReferenceCard(backendAnime.firstOrNull(), onAnimeClick) }
            item { HomeAnimeSection("Continue Watching", backendAnime, onAnimeClick, showProgress = true) }
            item { HomeAnimeSection("Trending", backendAnime, onAnimeClick) }
            item { HomeAnimeSection("New Updates", backendAnime.sortedByDescending { it.latestEpisode }, onAnimeClick, badge = "NEW") }
            item { HomeAnimeSection("Anime Completed", backendAnime.filter { it.status.equals("Finished", ignoreCase = true) }, onAnimeClick, badge = "COMPLETED") }
            item { HomeAnimeSection("Recommended", backendAnime.reversed(), onAnimeClick) }
            item { HomePremiumReferenceCard() }
        }
    }
}

@Composable
private fun HomeReferenceHeader(search: String, onSearchChanged: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("KA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("KakaAnime", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Temukan anime berikutnya", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)) {
                Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan", modifier = Modifier.padding(11.dp))
            }
        }

        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)) {
            Row(Modifier.padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = search,
                    onValueChange = onSearchChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { inner ->
                        if (search.isEmpty()) Text("Cari anime...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
private fun FeaturedReferenceCard(anime: Anime?, onClick: (Anime) -> Unit) {
    if (anime == null) return
    Box(Modifier.fillMaxWidth().height(285.dp).clip(RoundedCornerShape(26.dp)).clickable { onClick(anime) }.background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background)))) {
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("FEATURED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(anime.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text("★ ${anime.rating}  •  ${anime.status}  •  ${anime.year}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(9.dp))
            Text(anime.genre, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun HomeAnimeSection(title: String, animeList: List<Anime>, onClick: (Anime) -> Unit, badge: String? = null, showProgress: Boolean = false) {
    if (animeList.isEmpty()) return
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Lihat semua", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(animeList) { anime -> ReferencePosterCard(anime, onClick, badge, showProgress) }
        }
    }
}

@Composable
private fun ReferencePosterCard(anime: Anime, onClick: (Anime) -> Unit, badge: String?, showProgress: Boolean) {
    Column(Modifier.width(136.dp).clickable { onClick(anime) }) {
        Box(Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .12f), MaterialTheme.colorScheme.surface))))
            Text("POSTER", Modifier.align(Alignment.Center), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (badge != null) {
                Surface(Modifier.align(Alignment.TopStart).padding(8.dp), RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .9f)) {
                    Text(badge, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            if (showProgress) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.height(7.dp))
        Text(anime.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("Ep ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun SearchAnimeRow(anime: Anime, onClick: (Anime) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)).clickable { onClick(anime) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(64.dp, 86.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Text("POSTER", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(anime.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(anime.genre, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Text("Ep ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HomePremiumReferenceCard() {
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) {
        Column(Modifier.padding(18.dp)) {
            Text("PREMIUM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(3.dp))
            Text("1080p • Auto Skip • Offline", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Fitur premium KakaAnime akan terhubung di tahap berikutnya.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyHomeState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Text("Anime tidak ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
