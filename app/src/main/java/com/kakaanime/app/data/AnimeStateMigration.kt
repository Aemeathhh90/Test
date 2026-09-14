package com.kakaanime.app.data

/**
 * Catalog-aware migration helpers. Legacy title state is only migrated when a title
 * resolves to exactly one anime group and, for episode state, exactly one season.
 * This avoids accidentally sharing progress between seasons.
 */
object AnimeStateMigration {
    data class CatalogEntry(
        val title: String,
        val animeGroupId: String,
        val seasonNumber: Int? = null,
        val seasonTitle: String? = null,
    )

    fun resolveUniqueGroupId(
        legacyTitle: String,
        catalog: List<CatalogEntry>,
    ): String? {
        val normalized = legacyTitle.trim().lowercase()
        if (normalized.isBlank()) return null
        val groups = catalog.asSequence()
            .filter { it.title.trim().lowercase() == normalized }
            .map { it.animeGroupId.trim() }
            .filter { it.isNotBlank() }
            .toSet()
        return groups.singleOrNull()
    }

    fun resolveUniqueSeason(
        legacyTitle: String,
        catalog: List<CatalogEntry>,
    ): CatalogEntry? {
        val groupId = resolveUniqueGroupId(legacyTitle, catalog) ?: return null
        return catalog.asSequence()
            .filter { it.animeGroupId.trim() == groupId }
            .distinctBy { seasonKey(it) }
            .singleOrNull()
    }

    fun migrateFavoriteTitles(
        legacyTitles: Set<String>,
        catalog: List<CatalogEntry>,
    ): Set<String> = legacyTitles.mapNotNull { resolveUniqueGroupId(it, catalog) }.toSet()

    fun migrateWatchedEpisodes(
        legacyEpisodes: Map<String, Int>,
        catalog: List<CatalogEntry>,
    ): Map<String, Int> = legacyEpisodes.mapNotNull { (title, episode) ->
        if (episode <= 0) return@mapNotNull null
        val season = resolveUniqueSeason(title, catalog) ?: return@mapNotNull null
        val key = AnimeStateIdentity(season.animeGroupId, season.seasonNumber, season.seasonTitle)
            .let { watchedKey(it) }
        key to episode
    }.toMap()

    fun migrateUnlockedEpisodeKeys(
        legacyKeys: Set<String>,
        catalog: List<CatalogEntry>,
    ): Set<String> = legacyKeys.mapNotNull { key ->
        val separator = key.lastIndexOf("::")
        if (separator <= 0) return@mapNotNull null
        val title = key.substring(0, separator)
        val episode = key.substring(separator + 2).toIntOrNull() ?: return@mapNotNull null
        val season = resolveUniqueSeason(title, catalog) ?: return@mapNotNull null
        AnimeStateIdentity(season.animeGroupId, season.seasonNumber, season.seasonTitle).episodeKey(episode)
    }.toSet()

    fun migrateWatchHistory(
        legacyEntries: List<WatchHistoryEntry>,
        catalog: List<CatalogEntry>,
    ): List<SeasonAwareWatchHistoryEntry> = legacyEntries.mapNotNull { entry ->
        val season = resolveUniqueSeason(entry.title, catalog) ?: return@mapNotNull null
        SeasonAwareWatchHistoryEntry(
            animeGroupId = season.animeGroupId.trim(),
            seasonNumber = season.seasonNumber,
            seasonTitle = season.seasonTitle,
            episode = entry.episode,
            title = entry.title,
            episodeTitle = entry.episodeTitle,
            episodeThumbnailUrl = entry.episodeThumbnailUrl,
            watchedAt = entry.watchedAt,
            durationMs = entry.durationMs,
        )
    }

    private fun seasonKey(entry: CatalogEntry): String = buildString {
        append(entry.seasonNumber ?: "na")
        append("::")
        append(entry.seasonTitle?.trim()?.lowercase().orEmpty())
    }

    private fun watchedKey(identity: AnimeStateIdentity): String = buildString {
        append(identity.animeGroupId.trim())
        append("::season:")
        append(identity.seasonNumber ?: "na")
        append("::title:")
        append(identity.seasonTitle?.trim()?.lowercase().orEmpty())
    }
}
