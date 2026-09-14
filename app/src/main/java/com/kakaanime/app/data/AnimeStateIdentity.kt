package com.kakaanime.app.data

/** Stable identity for local anime state: Favorite is group-level, episode state is season-level. */
data class AnimeStateIdentity(
    val animeGroupId: String,
    val seasonNumber: Int? = null,
    val seasonTitle: String? = null,
) {
    fun favoriteKey(): String = animeGroupId.trim()

    fun episodeKey(episode: Int): String = buildString {
        append(animeGroupId.trim())
        append("::season:")
        append(seasonNumber ?: "na")
        append("::title:")
        append(seasonTitle?.trim()?.lowercase().orEmpty())
        append("::episode:")
        append(episode)
    }
}

/**
 * Legacy title state is intentionally kept separate until catalog context is available.
 * This prevents an unsafe automatic migration when multiple seasons share a title.
 */
object AnimeStateMigrationPolicy {
    const val LEGACY_TITLE_STATE = "title-based"
    const val SEASON_AWARE_STATE = "group-season-episode"

    fun canMigrateLegacyTitle(
        legacyTitle: String,
        matchingGroupIds: Set<String>,
    ): Boolean = legacyTitle.isNotBlank() && matchingGroupIds.size == 1
}
