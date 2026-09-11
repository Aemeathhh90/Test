package com.kakaanime.app.data

import com.kakaanime.app.provider.ProviderAnime
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType

object AnimeMapper {

    fun providerAnimeToAnimeData(anime: ProviderAnime): AnimeData {
        return AnimeData(
            id = anime.id,
            title = anime.title,
            alternativeTitles = anime.alternativeTitles,
            posterUrl = anime.posterUrl,
            backdropUrl = anime.backdropUrl,
            description = anime.description,
            type = anime.type,
            status = mapStatus(anime.status),
            year = anime.year,
            season = anime.season,
            genres = anime.genres,
            studio = anime.studio,
            rating = anime.rating,
            totalEpisodes = anime.totalEpisodes,
            latestEpisode = anime.latestEpisode,
            updatedAt = anime.updatedAt
        )
    }

    fun providerEpisodeToEpisodeData(
        animeId: String,
        episode: ProviderEpisode
    ): EpisodeData {
        return EpisodeData(
            id = episode.id,
            animeId = animeId,
            episodeNumber = episode.episodeNumber,
            title = episode.title,
            thumbnailUrl = episode.thumbnailUrl,
            isNew = episode.isNew,
            releasedAt = episode.releasedAt,
            durationSeconds = episode.durationSeconds
        )
    }

    fun providerStreamToProviderStream(
        stream: ProviderStream
    ): ProviderStream {
        return ProviderStream(
            providerId = stream.providerId,
            url = stream.url,
            quality = stream.quality,
            language = stream.language,
            subtitleLanguage = stream.subtitleLanguage,
            isM3u8 = stream.isM3u8,
            headers = stream.headers
        )
    }

    private fun mapStatus(status: String?): AnimeStatus {
        return when (status?.trim()?.uppercase()) {
            "ONGOING",
            "AIRING",
            "CURRENT" -> AnimeStatus.ONGOING

            "FINISHED",
            "COMPLETED",
            "COMPLETE" -> AnimeStatus.FINISHED

            "UPCOMING",
            "NOT_YET_AIRED",
            "NOT YET AIRED" -> AnimeStatus.UPCOMING

            else -> AnimeStatus.UNKNOWN
        }
    }
}
