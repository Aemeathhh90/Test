package com.kakaanime.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kakaanime.app.data.KakaAnimePreferences

/**
 * History tab entry point.
 * The richer persisted watch-history UI lives in AnimeWatchedScreen;
 * keep this wrapper so the existing MainActivity navigation contract stays stable.
 */
@Composable
fun LibraryScreen(
    title: String,
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    emptyText: String
) {
    val preferences = KakaAnimePreferences(LocalContext.current)
    AnimeWatchedScreen(
        p = preferences,
        animeList = animeList,
        onBack = {},
        onAnimeClick = onAnimeClick
    )
}
