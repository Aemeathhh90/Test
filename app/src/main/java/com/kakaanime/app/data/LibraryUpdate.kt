package com.kakaanime.app.data

data class LibraryUpdate(
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val releasedAt: Long,
    val isRead: Boolean = false
)

fun List<LibraryUpdate>.unreadForFavorites(favoriteIds: Set<String>): List<LibraryUpdate> =
    filter { !it.isRead && it.animeId in favoriteIds }
        .sortedByDescending { it.releasedAt }
