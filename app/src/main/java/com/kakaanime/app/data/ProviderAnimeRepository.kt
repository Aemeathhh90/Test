package com.kakaanime.app.data

import com.kakaanime.app.provider.NormalizedEpisodeStream
import com.kakaanime.app.provider.NormalizedStream
import com.kakaanime.app.provider.ProviderEngine
import com.kakaanime.app.provider.StreamQuality

class ProviderAnimeRepository(
    private val engine: ProviderEngine
) : AnimeRepository {

    override suspend fun searchAnime(
        query: String
    ): List<AnimeData> {
        return engine.search(query)
            .map(AnimeMapper::providerAnimeToAnimeData)
            .distinctBy { it.id }
    }

    override suspend fun getAnime(
        id: String
    ): AnimeData? {
        return engine.getAnime(id)
            ?.let(AnimeMapper::providerAnimeToAnimeData)
    }

    override suspend fun getEpisodes(
        animeId: String
    ): List<EpisodeData> {
        return engine.getEpisodes(animeId)
            .map { episode ->
                AnimeMapper.providerEpisodeToEpisodeData(
                    animeId = animeId,
                    episode = episode
                )
            }
            .sortedBy { it.episodeNumber }
    }

    override suspend fun getLatestUpdates(): List<AnimeData> {
        return searchAnime("")
    }

    suspend fun getEpisodeStreams(
        animeId: String,
        episodeNumber: Int
    ): NormalizedEpisodeStream? {
        return engine.getEpisodeStreams(
            animeId,
            episodeNumber
        )
    }

    suspend fun getBestStream(
        animeId: String,
        episodeNumber: Int,
        preferredQuality: StreamQuality? = null,
        isPremium: Boolean = false
    ): NormalizedStream? {
        return engine.getBestStream(
            animeId,
            episodeNumber,
            preferredQuality,
            isPremium
        )
    }
}
