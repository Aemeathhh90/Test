package com.kakaanime.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.AdMobRewardedAdGateway
import com.kakaanime.app.monetization.DiamondRules
import com.kakaanime.app.monetization.EpisodeGateDialog
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.premium.PremiumBillingState
import com.kakaanime.app.premium.PremiumScreen
import com.kakaanime.app.premium.PlayBillingGateway
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderPlaybackResolver
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaAnimeTheme
import com.kakaanime.app.ui.theme.KakaThemeState

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
    val outroEnd: Long = 0L,
    val animeGroupId: String = title,
    val seasonNumber: Int? = null,
    val seasonTitle: String? = null,
    val searchAliases: List<String> = emptyList(),
)

val localAnime = listOf(
    Anime("One Piece", 1140, "Action, Adventure, Fantasy", "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka menuju One Piece.", "Toei Animation", "Ongoing", "1999", "TV", "Ongoing", "9.0", 90L, 180L, 1380L, 1440L),
    Anime("Solo Leveling", 25, "Action, Fantasy", "Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.", "A-1 Pictures", "Season 2", "2025", "TV", "Finished", "8.8", 75L, 165L, 1380L, 1440L),
)

private enum class AnimeScreen { HOME, DETAIL, PLAYER, PREMIUM, WATCH_TOGETHER }
enum class BottomTab { HOME, CALENDAR, SOCIAL, LIBRARY, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KakaAnimeApp() }
    }
}

@Composable
fun KakaAnimeApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val preferences = remember(context) { KakaAnimePreferences(context) }
    val themeState = remember(preferences) {
        KakaThemeState(
            accent = runCatching { KakaAccent.valueOf(preferences.loadAccentName()) }.getOrDefault(KakaAccent.Blue),
            darkMode = preferences.loadDarkMode(),
        )
    }
    val rewardedAds = remember(context) { AdMobRewardedAdGateway(context) }

    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var catalogAnime by remember { mutableStateOf(localAnime) }
    var selectedEpisode by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showPremium by remember { mutableStateOf(false) }
    var showWatchTogether by remember { mutableStateOf(false) }
    var resolvedStreamUrl by remember { mutableStateOf<String?>(null) }
    var streamLoading by remember { mutableStateOf(false) }
    var streamRetry by remember { mutableIntStateOf(0) }
    var providerEpisodes by remember { mutableStateOf<List<ProviderEpisode>>(emptyList()) }
    var episodeListLoading by remember { mutableStateOf(false) }
    var homeRefreshKey by remember { mutableIntStateOf(0) }
    var streamFirstFrameRendered by remember { mutableStateOf(false) }

    var favoriteTitles by remember(preferences) { mutableStateOf(preferences.loadFavoriteTitles()) }
    var watchedEpisodes by remember(preferences) { mutableStateOf(preferences.loadWatchedEpisodes()) }
    var watchedEpisodeNumbers by remember(preferences) {
        mutableStateOf(preferences.loadWatchHistory().groupBy { it.title }.mapValues { (_, entries) -> entries.map { it.episode }.toSet() })
    }
    var unlockedEpisodes by remember(preferences) { mutableStateOf(preferences.loadUnlockedEpisodes()) }
    var monetizationState by remember(preferences) { mutableStateOf(MonetizationState(preferences.loadDiamonds(), preferences.loadPremium())) }
    var episodeGateTarget by remember { mutableStateOf<Pair<Anime, Int>?>(null) }
    var playerUnlockTarget by remember { mutableStateOf<Pair<Anime, Int>?>(null) }
    var playerUnlockRemaining by remember { mutableIntStateOf(0) }

    var premiumBillingState by remember { mutableStateOf<PremiumBillingState>(PremiumBillingState.Loading) }
    val playBillingGateway = remember(activity) {
        activity?.let { currentActivity ->
            PlayBillingGateway(
                currentActivity,
                onPremiumEntitled = {
                    monetizationState = monetizationState.copy(isPremium = true)
                    preferences.savePremium(true)
                },
                onBillingState = { premiumBillingState = it },
                onMessage = { premiumBillingState = PremiumBillingState.Unavailable(it) },
            )
        }
    }

    DisposableEffect(playBillingGateway) {
        playBillingGateway?.connectAndLoad()
        onDispose { playBillingGateway?.destroy() }
    }

    fun seasonsFor(anime: Anime): List<Anime> {
        val groupId = anime.animeGroupId.ifBlank { anime.title }
        val matches = catalogAnime.filter { candidate ->
            candidate.animeGroupId.ifBlank { candidate.title } == groupId
        }
        val source = if (matches.isEmpty()) listOf(anime) else matches + anime
        return source
            .distinctBy { it.seasonNumber ?: it.seasonTitle?.trim()?.lowercase() ?: it.title.trim().lowercase() }
            .sortedWith(compareBy(nullsLast<Int>()) { it.seasonNumber })
    }

    fun recordWatched(anime: Anime, episode: Int) {
        val providerEpisode = providerEpisodes.firstOrNull { it.number == episode }
        preferences.recordWatchedEpisode(anime.title, episode, providerEpisode?.title, providerEpisode?.thumbnailUrl)
        watchedEpisodes = watchedEpisodes + (anime.title to episode)
        preferences.saveWatchedEpisodes(watchedEpisodes)
        watchedEpisodeNumbers = watchedEpisodeNumbers.toMutableMap().apply {
            put(anime.title, (get(anime.title).orEmpty() + episode).toSet())
        }
        homeRefreshKey++
    }

    fun episodeKey(anime: Anime, episode: Int) = "${anime.title}::$episode"

    fun grantAndOpen(anime: Anime, episode: Int) {
        episodeGateTarget = null
        playerUnlockTarget = null
        playerUnlockRemaining = 0
        selectedAnime = anime
        selectedEpisode = episode
    }

    fun unlockFromReward(anime: Anime, episode: Int, diamondReward: Int) {
        val rewardedState = monetizationState.copy(diamonds = monetizationState.diamonds + diamondReward)
        val afterReward = DiamondRules.consumeForEpisode(rewardedState) ?: return
        monetizationState = afterReward
        preferences.saveDiamonds(afterReward.diamonds)
        val key = episodeKey(anime, episode)
        unlockedEpisodes = unlockedEpisodes + key
        preferences.markEpisodeUnlocked(anime.title, episode)
        grantAndOpen(anime, episode)
    }

    fun openEpisode(anime: Anime, episode: Int) {
        if (monetizationState.isPremium || episodeKey(anime, episode) in unlockedEpisodes) {
            grantAndOpen(anime, episode)
            return
        }
        val consumed = DiamondRules.consumeForEpisode(monetizationState)
        if (consumed != null) {
            monetizationState = consumed
            preferences.saveDiamonds(consumed.diamonds)
            val key = episodeKey(anime, episode)
            unlockedEpisodes = unlockedEpisodes + key
            preferences.markEpisodeUnlocked(anime.title, episode)
            grantAndOpen(anime, episode)
            return
        }
        episodeGateTarget = anime to episode
    }

    fun startPlayerUnlock(target: Pair<Anime, Int>, requestRewardedAd: Boolean) {
        episodeGateTarget = null
        playerUnlockTarget = target
        playerUnlockRemaining = 90
        selectedAnime = target.first
        selectedEpisode = target.second
        if (requestRewardedAd) {
            rewardedAds.show(
                onReward = { diamonds ->
                    if (playerUnlockTarget != target) return@show
                    unlockFromReward(target.first, target.second, diamonds)
                },
                onUnavailable = {},
            )
        }
    }

    fun cancelPlayerUnlock() {
        playerUnlockTarget = null
        playerUnlockRemaining = 0
        selectedEpisode = null
    }

    LaunchedEffect(playerUnlockTarget) {
        val target = playerUnlockTarget ?: run {
            playerUnlockRemaining = 0
            return@LaunchedEffect
        }
        playerUnlockRemaining = 90
        while (playerUnlockRemaining > 0 && playerUnlockTarget == target) {
            kotlinx.coroutines.delay(1000L)
            if (playerUnlockTarget == target) playerUnlockRemaining--
        }
        if (playerUnlockTarget == target && playerUnlockRemaining == 0) {
            unlockFromReward(target.first, target.second, DiamondRules.DIAMONDS_PER_REWARDED_AD)
        }
    }

    LaunchedEffect(selectedAnime?.title, selectedAnime?.seasonNumber, selectedAnime?.seasonTitle, selectedEpisode, monetizationState.isPremium, streamRetry) {
        val anime = selectedAnime
        val episode = selectedEpisode
        if (anime == null || episode == null) {
            resolvedStreamUrl = null
            streamLoading = false
            streamFirstFrameRendered = false
            return@LaunchedEffect
        }
        resolvedStreamUrl = null
        streamLoading = true
        streamFirstFrameRendered = false
        resolvedStreamUrl = runCatching {
            ProviderPlaybackResolver.resolve(
                anime.title,
                episode,
                monetizationState.isPremium,
                seasonNumber = anime.seasonNumber,
                seasonTitle = anime.seasonTitle,
            )?.url
        }.getOrNull()
        streamLoading = false
        if (resolvedStreamUrl != null) {
            kotlinx.coroutines.delay(15000L)
            if (streamFirstFrameRendered && selectedAnime?.title == anime.title && selectedAnime?.seasonNumber == anime.seasonNumber && selectedEpisode == episode) recordWatched(anime, episode)
        }
    }

    LaunchedEffect(selectedAnime?.title, selectedAnime?.seasonNumber, selectedAnime?.seasonTitle) {
        val anime = selectedAnime
        if (anime == null) {
            providerEpisodes = emptyList()
            episodeListLoading = false
            return@LaunchedEffect
        }
        episodeListLoading = true
        providerEpisodes = runCatching {
            ProviderPlaybackResolver.episodes(anime.title, seasonNumber = anime.seasonNumber, seasonTitle = anime.seasonTitle)
        }.getOrDefault(emptyList())
        episodeListLoading = false
        while (true) {
            kotlinx.coroutines.delay(10 * 60 * 1000L)
            if (selectedAnime?.title != anime.title || selectedAnime?.seasonNumber != anime.seasonNumber || selectedAnime?.seasonTitle != anime.seasonTitle) return@LaunchedEffect
            val refreshed = runCatching {
                ProviderPlaybackResolver.episodes(anime.title, seasonNumber = anime.seasonNumber, seasonTitle = anime.seasonTitle)
            }.getOrDefault(emptyList())
            if (refreshed.isNotEmpty()) providerEpisodes = refreshed
        }
    }

    val screen = when {
        showWatchTogether -> AnimeScreen.WATCH_TOGETHER
        showPremium -> AnimeScreen.PREMIUM
        selectedAnime != null && selectedEpisode != null -> AnimeScreen.PLAYER
        selectedAnime != null -> AnimeScreen.DETAIL
        else -> AnimeScreen.HOME
    }

    KakaAnimeTheme(themeState = themeState) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { (fadeIn() + slideInHorizontally { it / 8 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 10 }) },
            label = "screen_transition",
        ) { target ->
            when (target) {
                AnimeScreen.HOME -> Box(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition",
                        modifier = Modifier.fillMaxSize().padding(bottom = 84.dp),
                    ) { tab ->
                        when (tab) {
                            BottomTab.HOME -> ReDantotsuHomeScreen(localAnime, { selectedAnime = it }, { anime, episode -> openEpisode(anime, episode) }, homeRefreshKey) { loaded -> catalogAnime = loaded }
                            BottomTab.CALENDAR -> CalendarScreen(catalogAnime, { selectedAnime = it }, favoriteTitles)
                            BottomTab.SOCIAL -> SocialScreen(onOpenWatchTogether = { showWatchTogether = true })
                            BottomTab.LIBRARY -> LibraryTabsScreen(catalogAnime, { selectedAnime = it })
                            BottomTab.PROFILE -> ProfileScreen(themeState, monetizationState, { showPremium = true }) { selectedTab = BottomTab.LIBRARY }
                        }
                    }
                    KakaBottomNavigation(selectedTab) { selectedTab = it as BottomTab }
                }

                AnimeScreen.WATCH_TOGETHER -> WatchTogetherScreen(onBack = { showWatchTogether = false })

                AnimeScreen.DETAIL -> AnimeDetailScreen(
                    selectedAnime!!,
                    selectedAnime!!.title in favoriteTitles,
                    watchedEpisodes[selectedAnime!!.title],
                    watchedEpisodeNumbers[selectedAnime!!.title].orEmpty(),
                    providerEpisodes,
                    episodeListLoading,
                    { selectedAnime = null; selectedEpisode = null },
                    {
                        favoriteTitles = if (selectedAnime!!.title in favoriteTitles) favoriteTitles - selectedAnime!!.title else favoriteTitles + selectedAnime!!.title
                        preferences.saveFavoriteTitles(favoriteTitles)
                    },
                    { openEpisode(selectedAnime!!, it) },
                    seasonOptions = seasonsFor(selectedAnime!!),
                    onSeasonSelected = {
                        selectedAnime = it
                        selectedEpisode = null
                    },
                )

                AnimeScreen.PLAYER -> {
                    val anime = selectedAnime!!
                    val episode = selectedEpisode!!
                    val previousProviderEpisode = providerEpisodes.map { it.number }.filter { it < episode }.maxOrNull()
                    val nextProviderEpisode = providerEpisodes.map { it.number }.filter { it > episode }.minOrNull()
                    val latestEpisode = providerEpisodes.maxOfOrNull { it.number } ?: anime.latestEpisode
                    if (streamLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text("Mencari stream Episode $episode...", color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    } else if (resolvedStreamUrl != null) {
                        VideoPlayerScreen(
                            videoUrl = resolvedStreamUrl!!,
                            title = anime.title,
                            episodeNumber = episode,
                            description = anime.description,
                            introStart = anime.introStart,
                            introEnd = anime.introEnd,
                            outroStart = anime.outroStart,
                            outroEnd = anime.outroEnd,
                            isPremium = monetizationState.isPremium,
                            episodes = providerEpisodes,
                            watchedEpisodes = watchedEpisodeNumbers[anime.title].orEmpty(),
                            unlockRemainingSeconds = playerUnlockTarget?.takeIf { it == (anime to episode) }?.let { playerUnlockRemaining },
                            onCancelUnlock = { cancelPlayerUnlock() },
                            modifier = Modifier.fillMaxSize(),
                            onBack = { if (playerUnlockTarget != null) cancelPlayerUnlock() else selectedEpisode = null },
                            onPreviousEpisode = { previousProviderEpisode?.let { openEpisode(anime, it) } },
                            onNextEpisode = { nextProviderEpisode?.takeIf { it <= latestEpisode }?.let { openEpisode(anime, it) } },
                            onEpisodeClick = { targetEpisode -> if (targetEpisode != episode) openEpisode(anime, targetEpisode) },
                            onRenderedFirstFrame = { streamFirstFrameRendered = true },
                        )
                    } else {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Stream tidak ditemukan", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Provider belum menemukan sumber untuk ${anime.title} Episode $episode.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { streamRetry++ }) { Text("Coba lagi") }
                            }
                        }
                    }
                }

                AnimeScreen.PREMIUM -> PremiumScreen(
                    state = monetizationState,
                    billingState = premiumBillingState,
                    onBack = { showPremium = false },
                    onSubscribe = { basePlanId ->
                        playBillingGateway?.launchPurchase(basePlanId) ?: run { premiumBillingState = PremiumBillingState.Unavailable("Google Play tidak tersedia di perangkat ini.") }
                    },
                    onRetryBilling = { playBillingGateway?.refresh() },
                )
            }
        }

        episodeGateTarget?.let { (anime, episode) ->
            val providerEpisode = providerEpisodes.firstOrNull { it.number == episode }
            EpisodeGateDialog(
                episodeNumber = episode,
                episodeTitle = providerEpisode?.title,
                episodeThumbnailUrl = providerEpisode?.thumbnailUrl,
                episodeReleasedAt = providerEpisode?.releasedAt,
                state = monetizationState,
                onDismiss = { episodeGateTarget = null },
                onWatchAdAndUnlock = { startPlayerUnlock(anime to episode, requestRewardedAd = true) },
                onWaitForUnlock = { startPlayerUnlock(anime to episode, requestRewardedAd = false) },
                onStartPremium = { episodeGateTarget = null; showPremium = true },
            )
        }
    }
}