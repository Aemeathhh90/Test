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
            season = null,
            genres = anime.genres,
            studio = null,
            rating = anime.rating,
            totalEpisodes = anime.latestEpisode,
            latestEpisode = anime.latestEpisode,
            updatedAt = 0L
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
            thumbnailUrl = null,
            isNew = episode.isNew,
            releasedAt = episode.releasedAt,
            durationSeconds = null
        )
    }

    private fun mapStatus(status: String?): AnimeStatus {
        return when (status?.trim()?.uppercase()) {
            "ONGOING", "AIRING", "CURRENT" -> AnimeStatus.ONGOING
            "FINISHED", "COMPLETED", "COMPLETE" -> AnimeStatus.FINISHED
            "UPCOMING", "NOT_YET_AIRED", "NOT YET AIRED" -> AnimeStatus.UPCOMING
            else -> AnimeStatus.UNKNOWN
        }
    }
}
