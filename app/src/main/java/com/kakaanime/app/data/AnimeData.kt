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

    val updatedAt: Long = 0L
)

enum class AnimeStatus {
    ONGOING,
    FINISHED,
    UPCOMING,
    UNKNOWN
}
