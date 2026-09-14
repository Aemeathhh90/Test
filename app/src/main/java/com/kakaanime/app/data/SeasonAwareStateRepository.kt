package com.kakaanime.app.data

/**
 * Small facade over KakaAnimePreferences for the canonical local identity model.
 * UI layers can use this without knowing the storage keys.
 */
class SeasonAwareStateRepository(private val preferences: KakaAnimePreferences) {
    fun identity(animeGroupId: String, seasonNumber: Int?, seasonTitle: String?): AnimeStateIdentity =
        AnimeStateIdentity(animeGroupId.trim(), seasonNumber, seasonTitle)

    fun isFavorite(animeGroupId: String): Boolean =
        preferences.isFavoriteGroup(animeGroupId)

    fun setFavorite(animeGroupId: String, favorite: Boolean) {
        preferences.setFavoriteGroup(animeGroupId, favorite)
    }

    fun watchedEpisode(identity: AnimeStateIdentity): Int? =
        preferences.loadWatchedEpisode(identity)

    fun recordWatched(identity: AnimeStateIdentity, episode: Int) {
        preferences.recordWatchedEpisode(identity, episode)
    }

    fun isUnlocked(identity: AnimeStateIdentity, episode: Int): Boolean =
        preferences.isEpisodeUnlocked(identity, episode)

    fun markUnlocked(identity: AnimeStateIdentity, episode: Int) {
        preferences.markEpisodeUnlocked(identity, episode)
    }

    fun history(): List<SeasonAwareWatchHistoryEntry> =
        preferences.loadWatchHistorySeasonAware()

    fun recordHistory(
        identity: AnimeStateIdentity,
        title: String,
        episode: Int,
        episodeTitle: String? = null,
        episodeThumbnailUrl: String? = null,
        durationMs: Long = 0L,
    ) {
        preferences.recordWatchHistory(
            identity = identity,
            title = title,
            episode = episode,
            episodeTitle = episodeTitle,
            episodeThumbnailUrl = episodeThumbnailUrl,
            durationMs = durationMs,
        )
    }

    /**
     * Delete from both canonical season-aware history and the legacy title-based
     * history. This prevents a deleted legacy item from reappearing through the
     * Library migration fallback.
     */
    fun deleteHistory(identity: AnimeStateIdentity, episode: Int, legacyTitle: String? = null) {
        preferences.deleteWatchHistoryEntry(identity, episode)
        legacyTitle?.trim()?.takeIf { it.isNotBlank() }?.let {
            preferences.deleteWatchHistoryEntry(it, episode)
        }
    }

    /** Clear both stores because Library currently reads both during migration. */
    fun clearHistory() {
        preferences.clearWatchHistorySeasonAware()
        preferences.clearWatchHistory()
    }
}
