package com.kakaanime.app.data

/**
 * Shared catalog status for an anime/season entry.
 *
 * HIATUS is intentionally distinct from ONGOING because a series can stop
 * temporarily without being finished.
 */
enum class AnimeStatus {
    ONGOING,
    FINISHED,
    HIATUS,
    UPCOMING,
    UNKNOWN,
}

data class AnimeData(
    val id: String,
    val title: String,
    val animeGroupId: String = id,
    val seasonNumber: Int? = null,
    val seasonTitle: String? = null,
    val searchAliases: List<String> = emptyList(),
    val status: AnimeStatus = AnimeStatus.UNKNOWN,
)
