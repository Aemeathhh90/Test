package com.kakaanime.app.data

import com.kakaanime.app.provider.ProviderAnime
import com.kakaanime.app.provider.ProviderEpisode

object AnimeMapper {

    fun providerAnimeToAnimeData(anime: ProviderAnime): AnimeData {
        return AnimeData(
            id = anime.id,
            title = anime.title,
            alternativeTitles = anime.alternativeTitles,
            posterUrl = anime.posterUrl,
            backdropUrl = anime.backdropUrl,
            description = anime.description,
            type = "TV",
            status = mapStatus(anime.status),
            year = anime.year,
            season = anime.seasonTitle,
            genres = anime.genres,
            studio = null,
            rating = anime.rating,
            totalEpisodes = anime.latestEpisode,
            latestEpisode = anime.latestEpisode,
            updatedAt = 0L,
            animeGroupId = anime.animeGroupId,
            seasonNumber = anime.seasonNumber,
            seasonTitle = anime.seasonTitle,
            searchAliases = anime.searchAliases,
        )
    }

    fun providerEpisodeToEpisodeData(
        animeId: String,
        episode: ProviderEpisode
    ): EpisodeData {
        return EpisodeData(
            id = episode.id,
            animeId = animeId,
            episodeNumber = episode.number,
            title = episode.title,
            thumbnailUrl = episode.thumbnailUrl,
            isNew = episode.isNew,
            releasedAt = episode.releasedAt,
            durationSeconds = null,
            availability = when (episode.availability) {
                com.kakaanime.app.provider.EpisodeAvailability.AVAILABLE -> EpisodeAvailability.AVAILABLE
                com.kakaanime.app.provider.EpisodeAvailability.NOT_AVAILABLE -> EpisodeAvailability.NOT_AVAILABLE
                com.kakaanime.app.provider.EpisodeAvailability.NOT_RELEASED -> EpisodeAvailability.NOT_RELEASED
            }
        )
    }

    private fun mapStatus(status: String?): AnimeStatus {
        return when (status?.trim()?.uppercase()) {
            "ONGOING", "AIRING", "CURRENT" -> AnimeStatus.ONGOING
            "FINISHED", "COMPLETED", "COMPLETE" -> AnimeStatus.FINISHED
            "HIATUS", "ON_HIATUS", "ON HIATUS" -> AnimeStatus.HIATUS
            "UPCOMING", "NOT_YET_AIRED", "NOT YET AIRED" -> AnimeStatus.UPCOMING
            else -> AnimeStatus.UNKNOWN
        }
    }
}
