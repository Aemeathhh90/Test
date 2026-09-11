package com.kakaanime.app.data

import com.kakaanime.app.provider.NormalizedEpisodeStream
import com.kakaanime.app.provider.NormalizedStream
import com.kakaanime.app.provider.StreamQuality

class AnimeCatalogService(
    private val repository: ProviderAnimeRepository
) {

    suspend fun search(
        query: String
    ): List<AnimeData> {
        return repository.searchAnime(query)
    }

    suspend fun getAnime(
        animeId: String
    ): AnimeData? {
        return repository.getAnime(animeId)
    }

    suspend fun getEpisodes(
        animeId: String
    ): List<EpisodeData> {
        return repository.getEpisodes(animeId)
    }

    suspend fun getLatestUpdates(): List<AnimeData> {
        return repository.getLatestUpdates()
    }

    suspend fun getEpisodeStreams(
        animeId: String,
        episodeNumber: Int
    ): NormalizedEpisodeStream? {
        return repository.getEpisodeStreams(
            animeId = animeId,
            episodeNumber = episodeNumber
        )
    }

    suspend fun getBestStream(
        animeId: String,
        episodeNumber: Int,
        preferredQuality: StreamQuality? = null,
        isPremium: Boolean = false
    ): NormalizedStream? {
        return repository.getBestStream(
            animeId = animeId,
            episodeNumber = episodeNumber,
            preferredQuality = preferredQuality,
            isPremium = isPremium
        )
    }
}
