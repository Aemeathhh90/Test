package com.kakaanime.app.data

data class ProviderStream(
    val providerId: String,
    val url: String,

    val quality: String? = null,

    val language: String? = null,

    val subtitleLanguage: String? = null,

    val isM3u8: Boolean = false,

    val headers: Map<String, String> = emptyMap()
)

data class EpisodeStreams(
    val animeId: String,
    val episodeNumber: Int,
    val streams: List<ProviderStream> = emptyList()
)
