package com.kakaanime.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.MonetizationState

/** Compatibility entry point for MainActivity; Home V1 owns presentation. */
@Composable
fun ReDantotsuHomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onContinueWatchingClick: (Anime, Int) -> Unit = { anime, _ -> onAnimeClick(anime) },
    refreshKey: Int = 0,
    onAnimeCatalogLoaded: (List<Anime>) -> Unit = {},
) {
    val context = LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }

    HomeV1IntegrationScreen(
        fallbackAnime = animeList,
        monetizationState = MonetizationState(
            diamonds = preferences.loadDiamonds(),
            isPremium = preferences.loadPremium(),
        ),
        refreshKey = refreshKey,
        preferences = preferences,
        onCatalogLoaded = onAnimeCatalogLoaded,
        onAnimeClick = onAnimeClick,
        onContinueWatchingClick = onContinueWatchingClick,
        onProfileClick = {},
        onNotificationsClick = {},
        onDiamondClick = {},
        onPremiumClick = {},
        onWatchTogetherClick = {},
    )
}
