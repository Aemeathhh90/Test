package com.kakaanime.app.data

enum class EpisodeAvailability {
    AVAILABLE,
    NOT_AVAILABLE,
    NOT_RELEASED
}

data class EpisodeData(
    val id: String,
    val animeId: String,

    val episodeNumber: Int,
    val title: String? = null,

    val thumbnailUrl: String? = null,

    val isNew: Boolean = false,

    val releasedAt: Long? = null,

    val durationSeconds: Long? = null,

    /**
     * Availability is kept separate from provider presence so an already
     * released episode missing from the provider is not mislabelled as
     * "not released".
     */
    val availability: EpisodeAvailability = EpisodeAvailability.AVAILABLE
)
