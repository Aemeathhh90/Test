package com.kakaanime.app

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.SeasonAwareStateRepository
import com.kakaanime.app.ui.home.HomeAnimeUi
import com.kakaanime.app.ui.library.LibraryScreen
import com.kakaanime.app.ui.library.LibraryUiState

/** Compatibility entry point for MainActivity; Library V1 owns presentation. */
@Composable
fun LibraryTabsScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) { KakaAnimePreferences(context) }
    val stateRepository = remember(preferences) { SeasonAwareStateRepository(preferences) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val favoriteGroupIds = remember(animeList, refreshKey) {
        val storedIds = preferences.loadFavoriteGroupIds()
        val legacyTitles = preferences.loadFavoriteTitles()
        storedIds + animeList
            .filter { it.title in legacyTitles }
            .map { it.animeGroupId.ifBlank { it.title } }
    }

    val favoriteAnime = remember(animeList, favoriteGroupIds) {
        animeList
            .filter { (it.animeGroupId.ifBlank { it.title }) in favoriteGroupIds }
            .distinctBy { it.animeGroupId.ifBlank { it.title } }
    }

    fun toUi(anime: Anime) = HomeAnimeUi(
        id = anime.animeGroupId.ifBlank { anime.title },
        title = anime.title,
        latestEpisode = anime.latestEpisode,
        rating = anime.rating,
        genre = anime.genre,
        posterUrl = null,
        status = anime.status,
    )

    LibraryScreen(
        state = LibraryUiState(favorites = favoriteAnime.map(::toUi)),
        onAnimeClick = { selected ->
            favoriteAnime.firstOrNull { it.animeGroupId.ifBlank { it.title } == selected.id }
                ?.let(onAnimeClick)
        },
        onFavoriteToggle = { selected ->
            stateRepository.setFavorite(selected.id, false)
            refreshKey++
        },
    )
}
