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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            transitionSpec = { (fadeIn() + slideInHorizontally { it / 8 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 10 }) },
            label = "screen_transition"
        ) { target ->
            when (target) {
                AnimeScreen.HOME -> Box(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition",
                        modifier = Modifier.fillMaxSize().padding(bottom = 84.dp)
                    ) { tab ->
                        when (tab) {
                            BottomTab.HOME -> ReDantotsuHomeScreen(localAnime) { selectedAnime = it }
                            BottomTab.CALENDAR -> SimpleTabScreen("Calendar", "Jadwal anime akan terhubung di tahap V1 berikutnya.")
                            BottomTab.HISTORY -> SimpleTabScreen("History", "Riwayat tontonan akan terhubung di tahap V1 berikutnya.")
                            BottomTab.FAVORITE -> SimpleTabScreen("Favorite", "Anime favorit akan terhubung di tahap V1 berikutnya.")
                            BottomTab.PROFILE -> SimpleTabScreen("Profile", "Pengaturan dan profil akan terhubung di tahap V1 berikutnya.")
                        }
                    }
                    KakaBottomNavigation(selectedTab) { selectedTab = it }
                }
                AnimeScreen.DETAIL -> AnimeDetailScreen(
                    anime = selectedAnime!!,
                    onBack = { selectedAnime = null; selectedEpisode = null },
                    onEpisodeClick = { selectedEpisode = it }
                )
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
private fun KakaBottomNavigation(selectedTab: BottomTab, onTabSelected: (BottomTab) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .94f),
        tonalElevation = 5.dp,
        shadowElevation = 10.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomTab.entries.forEach { tab ->
                Column(
                    Modifier.weight(1f).clickable { onTabSelected(tab) }.padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.symbol, fontSize = if (selectedTab == tab) 22.sp else 20.sp, color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tab.label, fontSize = 10.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SimpleTabScreen(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AnimeDetailScreen(anime: Anime, onBack: () -> Unit, onEpisodeClick: (Int) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("‹ Kembali", modifier = Modifier.clickable { onBack() }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(anime.title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("★ ${anime.rating}  •  ${anime.status}  •  ${anime.year}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(anime.description)
            Spacer(Modifier.height(10.dp))
            Button(onClick = { onEpisodeClick(anime.latestEpisode) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("▶  Tonton Episode ${anime.latestEpisode}")
            }
            Spacer(Modifier.height(8.dp))
            Text("Episode", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        items((anime.latestEpisode downTo maxOf(1, anime.latestEpisode - 19)).toList()) { episode ->
            Surface(
                Modifier.fillMaxWidth().clickable { onEpisodeClick(episode) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
            ) {
                Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Episode $episode", fontWeight = FontWeight.Medium)
                    if (episode == anime.latestEpisode) Text("BARU", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
