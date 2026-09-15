package com.kakaanime.app.provider

/**
 * Compatibility facade for the app layer.
 *
 * The provider implementation now lives in the standalone `vider` library.
 * This facade keeps the existing app-facing package/API stable while the
 * migration is completed without duplicating provider behavior.
 */
object ProviderPlaybackResolver {
    suspend fun resolve(
        title: String,
        episodeNumber: Int,
        premium: Boolean,
        preferredQuality: StreamQuality? = null,
        seasonNumber: Int? = null,
        seasonTitle: String? = null,
    ): NormalizedStream? {
        val resolved = com.kakaanime.provider.ProviderPlaybackResolver.resolve(
            title = title,
            episodeNumber = episodeNumber,
            premium = premium,
            preferredQuality = preferredQuality?.toLibraryQuality(),
            seasonNumber = seasonNumber,
            seasonTitle = seasonTitle,
        ) ?: return null

        return NormalizedStream(
            providerId = resolved.providerId,
            url = resolved.url,
            quality = resolved.quality.toAppQuality(),
            type = resolved.type.toAppStreamType(),
            language = resolved.language,
            subtitleLanguage = resolved.subtitleLanguage,
            headers = resolved.headers,
            isPremium = resolved.isPremium,
        )
    }

    suspend fun episodes(
        title: String,
        seasonNumber: Int? = null,
        seasonTitle: String? = null,
    ): List<ProviderEpisode> =
        com.kakaanime.provider.ProviderPlaybackResolver.episodes(
            title = title,
            seasonNumber = seasonNumber,
            seasonTitle = seasonTitle,
        ).map { episode ->
            ProviderEpisode(
                id = episode.id,
                animeId = episode.animeId,
                number = episode.number,
                providerId = episode.providerId,
                title = episode.title,
                thumbnailUrl = episode.thumbnailUrl,
                isNew = episode.isNew,
                releasedAt = episode.releasedAt,
                animeGroupId = episode.animeGroupId,
                seasonNumber = episode.seasonNumber,
                seasonTitle = episode.seasonTitle,
                availability = when (episode.availability) {
                    com.kakaanime.provider.EpisodeAvailability.AVAILABLE -> EpisodeAvailability.AVAILABLE
                    com.kakaanime.provider.EpisodeAvailability.NOT_AVAILABLE -> EpisodeAvailability.NOT_AVAILABLE
                    com.kakaanime.provider.EpisodeAvailability.NOT_RELEASED -> EpisodeAvailability.NOT_RELEASED
                },
            )
        }

    private fun StreamQuality.toLibraryQuality(): com.kakaanime.provider.StreamQuality = when (this) {
        StreamQuality.Q360 -> com.kakaanime.provider.StreamQuality.Q360
        StreamQuality.Q480 -> com.kakaanime.provider.StreamQuality.Q480
        StreamQuality.Q720 -> com.kakaanime.provider.StreamQuality.Q720
        StreamQuality.Q1080 -> com.kakaanime.provider.StreamQuality.Q1080
        StreamQuality.UNKNOWN -> com.kakaanime.provider.StreamQuality.UNKNOWN
    }

    private fun com.kakaanime.provider.StreamQuality.toAppQuality(): StreamQuality = when (this) {
        com.kakaanime.provider.StreamQuality.Q360 -> StreamQuality.Q360
        com.kakaanime.provider.StreamQuality.Q480 -> StreamQuality.Q480
        com.kakaanime.provider.StreamQuality.Q720 -> StreamQuality.Q720
        com.kakaanime.provider.StreamQuality.Q1080 -> StreamQuality.Q1080
        com.kakaanime.provider.StreamQuality.UNKNOWN -> StreamQuality.UNKNOWN
    }

    private fun com.kakaanime.provider.StreamType.toAppStreamType(): StreamType = when (this) {
        com.kakaanime.provider.StreamType.HLS -> StreamType.HLS
        com.kakaanime.provider.StreamType.DASH -> StreamType.DASH
        com.kakaanime.provider.StreamType.MP4 -> StreamType.MP4
        com.kakaanime.provider.StreamType.UNKNOWN -> StreamType.UNKNOWN
    }
}
