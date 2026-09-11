package com.kakaanime.app.ui.home

import com.kakaanime.app.data.AnimeData

object HomeAnimeAdapter {

    fun latestEpisode(anime: AnimeData): Int {
        return anime.latestEpisode ?: 0
    }

    fun genre(anime: AnimeData): String {
        return anime.genres
            .take(2)
            .joinToString(" • ")
            .ifBlank { "Anime" }
    }

    fun rating(anime: AnimeData): Double {
        return anime.rating ?: 0.0
    }

    fun displayTitle(anime: AnimeData): String {
        return anime.title
    }
}
