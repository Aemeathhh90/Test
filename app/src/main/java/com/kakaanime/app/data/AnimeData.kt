package com.kakaanime.app.data

data class AnimeData(
    val id: String,
    val title: String,
    val alternativeTitles: List<String> = emptyList(),

    val posterUrl: String? = null,
    val backdropUrl: String? = null,

    val description: String = "",

    val type: String = "TV",
    val status: AnimeStatus = AnimeStatus.UNKNOWN,

    val year: Int? = null,
    val season: String? = null,

    val genres: List<String> = emptyList(),

    val studio: String? = null,

    val rating: Double? = null,

    val totalEpisodes: Int? = null,
    val latestEpisode: Int? = null,

    val updatedAt: Long = 0L,

    // Season-aware catalog identity. Defaults keep existing repository/provider mappings compatible.
    val animeGroupId: String = id,
    val seasonNumber: Int? = null,
    val seasonTitle: String? = null,
    val searchAliases: List<String> = emptyList(),
)

enum class AnimeStatus {
    ONGOING,
    FINISHED,
    UPCOMING,
    UNKNOWN
}
