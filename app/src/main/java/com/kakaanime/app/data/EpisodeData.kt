package com.kakaanime.app.data

data class EpisodeData(
    val id: String,
    val animeId: String,

    val episodeNumber: Int,
    val title: String? = null,

    val thumbnailUrl: String? = null,

    val isNew: Boolean = false,

    val releasedAt: Long? = null,

    val durationSeconds: Long? = null
)
