package com.kakaanime.app.ui.home

import com.kakaanime.app.data.AnimeData
import com.kakaanime.app.ui.theme.KakaTokens

/** Maps domain/catalog data into the UI-only Home model. */
fun AnimeData.toHomeAnimeUi(): HomeAnimeUi = HomeAnimeUi(
    id = title,
    title = HomeAnimeAdapter.displayTitle(this),
    latestEpisode = HomeAnimeAdapter.latestEpisode(this),
    rating = HomeAnimeAdapter.rating(this).takeIf { it > 0.0 }?.toString() ?: "-",
    genre = HomeAnimeAdapter.genre(this),
    posterUrl = null,
    status = "Ongoing",
)

@Suppress("UNUSED_PARAMETER")
private fun homeUiTokenReference() = KakaTokens.screenPadding
