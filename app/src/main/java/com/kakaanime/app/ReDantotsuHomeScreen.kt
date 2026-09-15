package com.kakaanime.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.MonetizationState

/**
 * Compatibility entry point kept for MainActivity while Home V1 is integrated.
 * The legacy home implementation is intentionally replaced only at this
 * presentation boundary; provider, playback, and catalog logic remain outside
 * the UI screen.
 */
@Composable
fun ReDantotsuHomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onContinueWatchingClick: (Anime, Int) -> Unit = { anime, _ -> onAnimeClick(anime) },
    refreshKey: Int = 0,
    onAnimeCatalogLoaded: (List<Anime>) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }
    var catalog by remember(animeList) { mutableStateOf(animeList) }

    LaunchedEffect(animeList, refreshKey) {
        catalog = com.kakaanime.app.network.AnimeRepository.loadAnime(animeList)
        onAnimeCatalogLoaded(catalog)
    }

    HomeV1IntegrationScreen(
        fallbackAnime = catalog,
        monetizationState = MonetizationState(
            diamonds = preferences.loadDiamonds(),
            isPremium = preferences.loadPremium(),
        ),
        refreshKey = refreshKey,
        preferences = preferences,
        onCatalogLoaded = { loaded ->
            catalog = loaded
            onAnimeCatalogLoaded(loaded)
        },
        onAnimeClick = onAnimeClick,
        onContinueWatchingClick = onContinueWatchingClick,
        onProfileClick = {},
        onNotificationsClick = {},
        onDiamondClick = {},
        onPremiumClick = {},
        onWatchTogetherClick = {},
    )
}
