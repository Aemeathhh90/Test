package com.kakaanime.app.provider

class ProviderEngine(
    private val registry: ProviderRegistry
) {

    private val router =
        SmartProviderRouter(registry)

    suspend fun search(
        query: String
    ): List<ProviderAnime> {

        return router.search(query)
    }

    suspend fun getAnime(
        animeId: String
    ): ProviderAnime? {

        return router.getAnime(animeId)
    }

    suspend fun getEpisodes(
        animeId: String
    ): List<ProviderEpisode> {

        return router.getEpisodes(animeId)
    }

    suspend fun getEpisodeStreams(
        animeId: String,
        episodeNumber: Int
    ): NormalizedEpisodeStream {

        val rawStreams =
            router.getStreams(
                animeId = animeId,
                episodeNumber = episodeNumber
            )

        val normalized =
            StreamNormalizer.normalize(
                rawStreams
            )

        return NormalizedEpisodeStream(
            animeId = animeId,
            episodeNumber = episodeNumber,
            streams = normalized
        )
    }

    suspend fun getBestStream(
        animeId: String,
        episodeNumber: Int,
        preferredQuality: StreamQuality? = null,
        premium: Boolean = false
    ): NormalizedStream? {

        val result =
            getEpisodeStreams(
                animeId = animeId,
                episodeNumber = episodeNumber
            )

        return StreamSelector.best(
            streams = result.streams,
            preferredQuality = preferredQuality,
            premium = premium
        )
    }
}
