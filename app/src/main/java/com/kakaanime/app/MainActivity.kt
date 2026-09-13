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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.AdMobRewardedAdGateway
import com.kakaanime.app.monetization.DiamondRules
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.premium.PremiumScreen
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderPlaybackResolver
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

val localAnime = listOf(
    Anime("One Piece", 1140, "Action, Adventure, Fantasy", "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka menuju One Piece.", "Toei Animation", "Ongoing", "1999", "TV", "Ongoing", "9.0", 90L, 180L, 1380L, 1440L),
    Anime("Solo Leveling", 25, "Action, Fantasy", "Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.", "A-1 Pictures", "Season 2", "2025", "TV", "Finished", "8.8", 75L, 165L, 1380L, 1440L)
)

private enum class AnimeScreen { HOME, DETAIL, PLAYER, PREMIUM }
private enum class BottomTab { HOME, CALENDAR, HISTORY, FAVORITE, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { KakaAnimeApp() } }
}

@Composable
fun KakaAnimeApp() {
    val themeState = rememberKakaThemeState()
    val context = LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }
    val rewardedAds = remember(context) { AdMobRewardedAdGateway(context) }
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var selectedEpisode by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showPremium by remember { mutableStateOf(false) }
    var resolvedStreamUrl by remember { mutableStateOf<String?>(null) }
    var streamLoading by remember { mutableStateOf(false) }
    var providerEpisodes by remember { mutableStateOf<List<ProviderEpisode>>(emptyList()) }
    var episodeListLoading by remember { mutableStateOf(false) }
    var favoriteTitles by remember(preferences) { mutableStateOf(preferences.loadFavoriteTitles()) }
    var watchedEpisodes by remember(preferences) { mutableStateOf(preferences.loadWatchedEpisodes()) }
    var monetizationState by remember(preferences) { mutableStateOf(MonetizationState(preferences.loadDiamonds(), preferences.loadPremium())) }

    fun recordWatched(anime: Anime, episode: Int) {
        val providerEpisode = providerEpisodes.firstOrNull { it.number == episode }
        preferences.recordWatchedEpisode(
            title = anime.title,
            episode = episode,
            episodeTitle = providerEpisode?.title,
            episodeThumbnailUrl = providerEpisode?.thumbnailUrl
        )
    }

    fun openEpisode(anime: Anime, episode: Int) {
        if (monetizationState.isPremium) {
            watchedEpisodes = watchedEpisodes + (anime.title to episode); preferences.saveWatchedEpisodes(watchedEpisodes); recordWatched(anime, episode); selectedAnime = anime; selectedEpisode = episode; return
        }
        val consumed = DiamondRules.consumeForEpisode(monetizationState)
        if (consumed != null) {
            monetizationState = consumed; watchedEpisodes = watchedEpisodes + (anime.title to episode); preferences.saveDiamonds(consumed.diamonds); preferences.saveWatchedEpisodes(watchedEpisodes); recordWatched(anime, episode); selectedAnime = anime; selectedEpisode = episode; return
        }
        rewardedAds.show(onReward = { diamonds ->
            val rewardedState = monetizationState.copy(diamonds = monetizationState.diamonds + diamonds); monetizationState = rewardedState
            val afterReward = DiamondRules.consumeForEpisode(rewardedState)
            if (afterReward != null) {
                monetizationState = afterReward; watchedEpisodes = watchedEpisodes + (anime.title to episode); preferences.saveDiamonds(afterReward.diamonds); preferences.saveWatchedEpisodes(watchedEpisodes); recordWatched(anime, episode); selectedAnime = anime; selectedEpisode = episode
            }
        }, onUnavailable = { })
    }

    LaunchedEffect(selectedAnime?.title, selectedEpisode, monetizationState.isPremium) {
        val anime = selectedAnime; val episode = selectedEpisode
        if (anime == null || episode == null) { resolvedStreamUrl = null; streamLoading = false; return@LaunchedEffect }
        resolvedStreamUrl = null; streamLoading = true
        resolvedStreamUrl = runCatching { ProviderPlaybackResolver.resolve(anime.title, episode, monetizationState.isPremium)?.url }.getOrNull()
        streamLoading = false
    }
    LaunchedEffect(selectedAnime?.title) {
        val anime = selectedAnime
        if (anime == null) { providerEpisodes = emptyList(); episodeListLoading = false; return@LaunchedEffect }
        episodeListLoading = true; providerEpisodes = runCatching { ProviderPlaybackResolver.episodes(anime.title) }.getOrDefault(emptyList()); episodeListLoading = false
    }
    val screen = when { showPremium -> AnimeScreen.PREMIUM; selectedAnime != null && selectedEpisode != null -> AnimeScreen.PLAYER; selectedAnime != null -> AnimeScreen.DETAIL; else -> AnimeScreen.HOME }

    KakaAnimeTheme(themeState = themeState) {
        AnimatedContent(targetState = screen, transitionSpec = { (fadeIn() + slideInHorizontally { it / 8 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 10 }) }, label = "screen_transition") { target ->
            when (target) {
                AnimeScreen.HOME -> Box(Modifier.fillMaxSize()) {
                    AnimatedContent(targetState = selectedTab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab_transition", modifier = Modifier.fillMaxSize().padding(bottom = 84.dp)) { tab ->
                        when (tab) {
                            BottomTab.HOME -> ReDantotsuHomeScreen(localAnime) { selectedAnime = it }
                            BottomTab.CALENDAR -> CalendarScreen(localAnime, { selectedAnime = it }, favoriteTitles)
                            BottomTab.HISTORY -> LibraryScreen("History", localAnime.filter { it.title in watchedEpisodes.keys }, { selectedAnime = it }, "Belum ada riwayat tontonan")
                            BottomTab.FAVORITE -> FavoriteScreen(localAnime.filter { it.title in favoriteTitles }, watchedEpisodes) { selectedAnime = it }
                            BottomTab.PROFILE -> ProfileScreen(themeState, monetizationState) { showPremium = true }
                        }
                    }
                    KakaBottomNavigation(selectedTab) { selectedTab = it }
                }
                AnimeScreen.DETAIL -> AnimeDetailScreen(selectedAnime!!, selectedAnime!!.title in favoriteTitles, watchedEpisodes[selectedAnime!!.title], providerEpisodes, episodeListLoading, { selectedAnime = null; selectedEpisode = null }, { favoriteTitles = if (selectedAnime!!.title in favoriteTitles) favoriteTitles - selectedAnime!!.title else favoriteTitles + selectedAnime!!.title; preferences.saveFavoriteTitles(favoriteTitles) }, { openEpisode(selectedAnime!!, it) })
                AnimeScreen.PLAYER -> {
                    val anime = selectedAnime!!; val episode = selectedEpisode!!
                    if (streamLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text("Mencari stream Episode $episode...", color = MaterialTheme.colorScheme.onBackground) } }
                    else if (resolvedStreamUrl != null) VideoPlayerScreen(resolvedStreamUrl!!, anime.title, episode, anime.description, anime.introStart, anime.introEnd, anime.outroStart, anime.outroEnd, monetizationState.isPremium, Modifier.fillMaxSize(), { if (episode > 1) { recordWatched(anime, episode - 1); selectedEpisode = episode - 1 } }, { if (episode < anime.latestEpisode) { recordWatched(anime, episode + 1); selectedEpisode = episode + 1 } })
                    else Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Stream tidak ditemukan", fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("Provider belum menemukan sumber untuk ${anime.title} Episode $episode.", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(16.dp)); Button(onClick = { selectedEpisode = episode }) { Text("Coba lagi") } } }
                }
                AnimeScreen.PREMIUM -> PremiumScreen(onBack = { showPremium = false })
            }
        }
    }
}
