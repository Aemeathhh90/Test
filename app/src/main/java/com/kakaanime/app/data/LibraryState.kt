package com.kakaanime.app.data

/**
 * V1 library state. Favorite is intentionally a single collection;
 * no duplicate completed/planned favorite categories.
 */
data class LibraryState(
    val favoriteIds: Set<String> = emptySet(),
    val watching: Map<String, Int> = emptyMap()
) {
    fun isFavorite(animeId: String): Boolean = animeId in favoriteIds

    fun toggleFavorite(animeId: String): LibraryState =
        if (animeId in favoriteIds) {
            copy(favoriteIds = favoriteIds - animeId)
        } else {
            copy(favoriteIds = favoriteIds + animeId)
        }

    fun markWatching(animeId: String, episode: Int): LibraryState =
        copy(watching = watching + (animeId to episode))
}
