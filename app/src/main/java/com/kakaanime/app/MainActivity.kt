package com.kakaanime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.ui.theme.KakaAnimeTheme
import com.kakaanime.app.ui.theme.rememberKakaThemeState

data class Anime(
    val title: String,
    val latestEpisode: Int,
    val genre: String,
    val description: String,
    val studio: String,
    val season: String,
    val year: String,
    val type: String,
    val status: String,
    val rating: String,
    val introStart: Long = 0L,
    val introEnd: Long = 0L,
    val outroStart: Long = 0L,
    val outroEnd: Long = 0L
)

private val localAnime = listOf(
    Anime("One Piece", 1140, "Action, Adventure, Fantasy", "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka menuju One Piece.", "Toei Animation", "Ongoing", "1999", "TV", "Ongoing", "9.0", 90L, 180L, 1380L, 1440L),
    Anime("Solo Leveling", 25, "Action, Fantasy", "Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.", "A-1 Pictures", "Season 2", "2025", "TV", "Finished", "8.8", 75L, 165L, 1380L, 1440L)
)

private enum class AnimeScreen { HOME, DETAIL, PLAYER }
private enum class BottomTab(val label: String, val symbol: String) {
    HOME("Home", "⌂"), CALENDAR("Calendar", "▣"), HISTORY("History", "◷"), FAVORITE("Favorite", "♡"), PROFILE("Profile", "●")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeState = rememberKakaThemeState()
            KakaAnimeTheme(themeState = themeState) { KakaAnimeApp() }
        }
    }
}

@Composable
fun KakaAnimeApp() {
    val themeState = rememberKakaThemeState()
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var selectedEpisode by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    val screen = when {
        selectedAnime != null && selectedEpisode != null -> AnimeScreen.PLAYER
        selectedAnime != null -> AnimeScreen.DETAIL
        else -> AnimeScreen.HOME
    }

    KakaAnimeTheme(themeState = themeState) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 8 }) togetherWith
                    (fadeOut() + slideOutHorizontally { -it / 10 })
            },
            label = "screen_transition"
        ) { target ->
            when (target) {
                AnimeScreen.HOME -> Box(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition",
                        modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)
                    ) { tab ->
                        when (tab) {
                            BottomTab.HOME -> HomeScreen(animeList = localAnime, onAnimeClick = { selectedAnime = it })
                            BottomTab.CALENDAR -> PlaceholderScreen("Calendar", "Jadwal anime berdasarkan hari dan tanggal.")
                            BottomTab.HISTORY -> PlaceholderScreen("History", "Anime dan episode yang terakhir kamu tonton.")
                            BottomTab.FAVORITE -> PlaceholderScreen("Favorite", "Anime favorit kamu akan muncul di sini.")
                            BottomTab.PROFILE -> PlaceholderScreen("Profile", "Pengaturan akun dan tampilan KakaAnime.")
                        }
                    }
                    KakaBottomNavigation(selectedTab, { selectedTab = it }, Modifier.align(Alignment.BottomCenter))
                }
                AnimeScreen.DETAIL -> AnimeDetailScreen(selectedAnime!!, { selectedAnime = null; selectedEpisode = null }) { selectedEpisode = it }
                AnimeScreen.PLAYER -> VideoPlayerScreen(
                    videoUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4",
                    introStart = selectedAnime!!.introStart,
                    introEnd = selectedAnime!!.introEnd,
                    outroStart = selectedAnime!!.outroStart,
                    outroEnd = selectedAnime!!.outroEnd,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun KakaBottomNavigation(selectedTab: BottomTab, onTabSelected: (BottomTab) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).clickable { onTabSelected(tab) }.padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.symbol, fontSize = if (selectedTab == tab) 23.sp else 21.sp, color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tab.label, fontSize = 10.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(animeList: List<Anime>, onAnimeClick: (Anime) -> Unit) {
    var search by remember { mutableStateOf("") }
    val filtered = animeList.filter { it.title.contains(search, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("KakaAnime", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Temukan tontonan berikutnya", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .58f))
                }
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .13f)) {
                    Text("KA", modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Cari anime, genre, karakter...") }
            )
        }

        if (search.isEmpty()) {
            item { FeaturedCard(animeList.firstOrNull(), onAnimeClick) }
            item { AnimeSection("Continue Watching", animeList, onAnimeClick, showProgress = true) }
            item { AnimeSection("Trending", animeList, onAnimeClick) }
            item { AnimeSection("New Updates", animeList.reversed(), onAnimeClick, badge = "NEW") }
            item { AnimeSection("Anime Completed", animeList.filter { it.status == "Finished" }, onAnimeClick, badge = "COMPLETED") }
            item { AnimeSection("Recommended", animeList.reversed(), onAnimeClick) }
            item { PremiumCard() }
        } else {
            item { Text("Hasil Pencarian", fontSize = 21.sp, fontWeight = FontWeight.Bold) }
            if (filtered.isEmpty()) item { EmptyState() } else items(filtered) { anime -> AnimeListCard(anime, onAnimeClick) }
        }
    }
}

@Composable
private fun FeaturedCard(anime: Anime?, onClick: (Anime) -> Unit) {
    if (anime == null) return
    Box(
        modifier = Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(28.dp)).clickable { onClick(anime) }
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)))
    ) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .18f), MaterialTheme.colorScheme.background.copy(alpha = .96f)))))
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("FEATURED", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(anime.title, fontSize = 29.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(5.dp))
            Text("★ ${anime.rating}   •   ${anime.status}   •   ${anime.year}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f))
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onClick(anime) }, shape = RoundedCornerShape(14.dp)) { Text("▶  Mulai Nonton") }
        }
    }
}

@Composable
private fun AnimeSection(title: String, animeList: List<Anime>, onClick: (Anime) -> Unit, badge: String? = null, showProgress: Boolean = false) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Lihat semua  ›", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(11.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            items(animeList) { anime -> AnimePosterCard(anime, onClick, badge, showProgress) }
        }
    }
}

@Composable
private fun AnimePosterCard(anime: Anime, onClick: (Anime) -> Unit, badge: String?, showProgress: Boolean) {
    Column(Modifier.width(142.dp).clickable { onClick(anime) }) {
        Box(Modifier.fillMaxWidth().height(188.dp).clip(RoundedCornerShape(17.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)))) {
            Text("POSTER", Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            if (badge != null) Surface(Modifier.align(Alignment.TopStart).padding(8.dp), shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .88f)) {
                Text(badge, Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
            if (showProgress) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.height(8.dp))
        Text(anime.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text("Ep ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun PremiumCard() {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("PREMIUM", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Nonton tanpa batas.", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("1080p • Auto Skip • Download Offline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {}, shape = RoundedCornerShape(13.dp)) { Text("Lihat Premium") }
        }
    }
}

@Composable
private fun AnimeListCard(anime: Anime, onClick: (Anime) -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick(anime) }, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 72.dp, height = 96.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text("POSTER", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(anime.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(anime.genre, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Episode ${anime.latestEpisode}  •  ★ ${anime.rating}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun EmptyState() { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("Anime tidak ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun PlaceholderScreen(title: String, subtitle: String) { Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
fun AnimeDetailScreen(anime: Anime, onBack: () -> Unit, onEpisodeClick: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("‹ Kembali", modifier = Modifier.clickable { onBack() }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp)); Text(anime.title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("★ ${anime.rating}  •  ${anime.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp)); Text(anime.description)
            Spacer(Modifier.height(10.dp)); Button(onClick = { onEpisodeClick(anime.latestEpisode) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("▶  Tonton Episode ${anime.latestEpisode}") }
            Spacer(Modifier.height(8.dp)); Text("Episode", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        items((anime.latestEpisode downTo maxOf(1, anime.latestEpisode - 19)).toList()) { episode ->
            Card(Modifier.fillMaxWidth().clickable { onEpisodeClick(episode) }, shape = RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Episode $episode", fontWeight = FontWeight.Medium); if (episode == anime.latestEpisode) Text("BARU", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
        }
    }
}
