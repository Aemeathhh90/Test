package com.kakaanime.app.data

/**
 * Catalog-aware migration helpers. Legacy title state is only migrated when a title
 * resolves to exactly one anime group, avoiding accidental cross-season state sharing.
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

    fun migrateFavoriteTitles(
        legacyTitles: Set<String>,
        catalog: List<CatalogEntry>,
    ): Set<String> = legacyTitles.mapNotNull { resolveUniqueGroupId(it, catalog) }.toSet()

    fun migrateUnlockedEpisodeKeys(
        legacyKeys: Set<String>,
        catalog: List<CatalogEntry>,
    ): Set<String> = legacyKeys.mapNotNull { key ->
        val separator = key.lastIndexOf("::")
        if (separator <= 0) return@mapNotNull null
        val title = key.substring(0, separator)
        val episode = key.substring(separator + 2).toIntOrNull() ?: return@mapNotNull null
        val groupId = resolveUniqueGroupId(title, catalog) ?: return@mapNotNull null
        val season = catalog.filter { it.animeGroupId == groupId }
            .distinctBy { it.seasonNumber ?: it.seasonTitle?.trim()?.lowercase() ?: "" }
        if (season.size != 1) return@mapNotNull null
        val entry = season.first()
        AnimeStateIdentity(groupId, entry.seasonNumber, entry.seasonTitle).episodeKey(episode)
    }.toSet()
}
