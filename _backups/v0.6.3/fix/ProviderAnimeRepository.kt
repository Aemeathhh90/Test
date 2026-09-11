package com.kakaanime.app.data

import com.kakaanime.app.provider.ProviderAnime
import com.kakaanime.app.provider.ProviderEngine
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderResult
import com.kakaanime.app.provider.NormalizedEpisodeStream

class ProviderAnimeRepository(
    private val engine: ProviderEngine
) : AnimeRepository {

    override suspend fun searchAnime(query: String): List<AnimeData> {
        return when (val result = engine.search(query)) {
            is ProviderResult.Success -> {
                result.data
                    .map(AnimeMapper::providerAnimeToAnimeData)
                    .distinctBy { it.id }
            }

            is ProviderResult.Empty -> emptyList()

            is ProviderResult.Error -> emptyList()
        }
    }

    override suspend fun getAnime(id: String): AnimeData? {
        return when (val result = engine.getAnime(id)) {
            is ProviderResult.Success -> {
                AnimeMapper.providerAnimeToAnimeData(result.data)
            }

            is ProviderResult.Empty -> null

            is ProviderResult.Error -> null
        }
    }

    override suspend fun getEpisodes(
        animeId: String
    ): List<EpisodeData> {
        return when (val result = engine.getEpisodes(animeId)) {
            is ProviderResult.Success -> {
                result.data
                    .map {
                        AnimeMapper.providerEpisodeToEpisodeData(
                            animeId = animeId,
                            episode = it
                        )
                    }
                    .sortedBy { it.episodeNumber }
            }

            is ProviderResult.Empty -> emptyList()

            is ProviderResult.Error -> emptyList()
        }
    }

    override suspend fun getLatestUpdates(): List<AnimeData> {
        return searchAnime("")
            .sortedByDescending { it.updatedAt }
    }

    suspend fun getEpisodeStreams(
        animeId: String,
        episodeNumber: Int
    ): NormalizedEpisodeStream? {
        return when (
            val result = engine.getEpisodeStreams(
                animeId = animeId,
                episodeNumber = episodeNumber
            )
        ) {
            is ProviderResult.Success -> result.data
            is ProviderResult.Empty -> null
            is ProviderResult.Error -> null
        }
    }

    suspend fun getBestStream(
        animeId: String,
        episodeNumber: Int,
        preferredQuality: Int? = null,
        isPremium: Boolean = false
    ): com.kakaanime.app.provider.NormalizedStream? {
        return when (
            val result = engine.getBestStream(
                animeId = animeId,
                episodeNumber = episodeNumber,
                preferredQuality = preferredQuality,
                isPremium = isPremium
            )
        ) {
            is ProviderResult.Success -> result.data
            is ProviderResult.Empty -> null
            is ProviderResult.Error -> null
        }
    }
}
