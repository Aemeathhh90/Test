package com.kakaanime.app.provider

class ProviderEngine(
    private val registry: ProviderRegistry
) {
    private val router = SmartProviderRouter(registry)

    suspend fun search(query: String): List<ProviderAnime> = router.search(query)

    suspend fun getAnime(animeId: String): ProviderAnime? = router.getAnime(animeId)

    suspend fun getEpisodes(animeId: String): List<ProviderEpisode> = router.getEpisodes(animeId)

    suspend fun getEpisodeStreams(
        animeId: String,
        episodeNumber: Int
    ): NormalizedEpisodeStream {
        val rawStreams = router.getStreams(animeId, episodeNumber)
        val providerPriorities = registry.all().associate { it.id to it.priority }
        val deduplicated = ProviderStreamDeduplicator.deduplicate(rawStreams, providerPriorities)
        val normalized = StreamNormalizer.normalize(deduplicated)

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
        val result = getEpisodeStreams(animeId, episodeNumber)
        return StreamSelector.best(
            streams = result.streams,
            preferredQuality = preferredQuality,
            premium = premium
        )
    }
}
