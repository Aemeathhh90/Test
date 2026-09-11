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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.premium.PremiumScreen
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

private enum class AnimeScreen { HOME, DETAIL, PLAYER, PREMIUM }
private enum class BottomTab { HOME, CALENDAR, HISTORY, FAVORITE, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KakaAnimeApp() }
    }
}

@Composable
fun KakaAnimeApp() {
    val themeState = rememberKakaThemeState()
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var selectedEpisode by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showPremium by remember { mutableStateOf(false) }
    var favoriteTitles by remember { mutableStateOf(setOf("One Piece")) }
    var watchedEpisodes by remember { mutableStateOf(mapOf("One Piece" to 1140)) }
    val monetizationState = remember { mutableStateOf(MonetizationState(diamonds = 0, isPremium = false)) }

    val screen = when {
        showPremium -> AnimeScreen.PREMIUM
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
                            BottomTab.CALENDAR -> CalendarScreen(localAnime, { selectedAnime = it }, favoriteTitles)
                            BottomTab.HISTORY -> LibraryScreen("History", localAnime.filter { it.title in watchedEpisodes.keys }, { selectedAnime = it }, "Belum ada riwayat tontonan")
                            BottomTab.FAVORITE -> LibraryScreen("Favorite", localAnime.filter { it.title in favoriteTitles }, { selectedAnime = it }, "Belum ada anime favorit")
                            BottomTab.PROFILE -> ProfileScreen(themeState, monetizationState.value) { showPremium = true }
                        }
                    }
                    KakaBottomNavigation(selectedTab) { selectedTab = it }
                }
                AnimeScreen.DETAIL -> AnimeDetailScreen(
                    anime = selectedAnime!!,
                    isFavorite = selectedAnime!!.title in favoriteTitles,
                    onBack = { selectedAnime = null; selectedEpisode = null },
                    onFavorite = {
                        favoriteTitles = if (selectedAnime!!.title in favoriteTitles) favoriteTitles - selectedAnime!!.title
                        else favoriteTitles + selectedAnime!!.title
                    },
                    onEpisodeClick = {
                        watchedEpisodes = watchedEpisodes + (selectedAnime!!.title to it)
                        selectedEpisode = it
                    }
                )
                AnimeScreen.PLAYER -> VideoPlayerScreen(
                    videoUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4",
                    introStart = selectedAnime!!.introStart,
                    introEnd = selectedAnime!!.introEnd,
                    outroStart = selectedAnime!!.outroStart,
                    outroEnd = selectedAnime!!.outroEnd,
                    modifier = Modifier.fillMaxSize()
                )
                AnimeScreen.PREMIUM -> PremiumScreen(
                    state = monetizationState.value,
                    onSubscribe = { /* Billing integration is intentionally kept for the final release pass. */ }
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
                val selected = selectedTab == tab
                val icon = when (tab) {
                    BottomTab.HOME -> Icons.Outlined.Home
                    BottomTab.CALENDAR -> Icons.Outlined.CalendarMonth
                    BottomTab.HISTORY -> Icons.Outlined.Schedule
                    BottomTab.FAVORITE -> Icons.Outlined.FavoriteBorder
                    BottomTab.PROFILE -> Icons.Outlined.Person
                }
                val label = when (tab) {
                    BottomTab.HOME -> "Home"
                    BottomTab.CALENDAR -> "Calendar"
                    BottomTab.HISTORY -> "History"
                    BottomTab.FAVORITE -> "Favorite"
                    BottomTab.PROFILE -> "Profile"
                }
                Column(
                    Modifier.weight(1f).clickable { onTabSelected(tab) }.padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AnimeDetailScreen(
    anime: Anime,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onEpisodeClick: (Int) -> Unit
) {
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { onEpisodeClick(anime.latestEpisode) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("▶  Tonton") }
                Button(onClick = onFavorite, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text(if (isFavorite) "♥ Favorit" else "♡ Favorit") }
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
