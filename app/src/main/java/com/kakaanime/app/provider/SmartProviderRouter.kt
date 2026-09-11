package com.kakaanime.app.provider

class SmartProviderRouter(
    private val registry: ProviderRegistry
) {

    suspend fun search(
        query: String
    ): List<ProviderAnime> {
        if (query.isBlank()) return emptyList()

        return registry.all()
            .flatMap { provider ->
                runCatching { provider.search(query.trim()) }
                    .getOrDefault(emptyList())
                    .map { it.copy(providerId = provider.id) }
            }
            .distinctBy { anime ->
                buildKey(anime.title, anime.year)
            }
    }

    suspend fun getAnime(
        animeId: String
    ): ProviderAnime? {
        if (animeId.isBlank()) return null

        return forEachProvider { provider ->
            provider.getAnime(animeId)?.copy(providerId = provider.id)
        }
    }

    suspend fun getEpisodes(
        animeId: String
    ): List<ProviderEpisode> {
        if (animeId.isBlank()) return emptyList()

        return forEachProvider { provider ->
            val episodes = provider.getEpisodes(animeId)
            if (episodes.isEmpty()) null
            else episodes
                .map { it.copy(providerId = provider.id) }
                .sortedBy { it.number }
        } ?: emptyList()
    }

    suspend fun getStreams(
        animeId: String,
        episodeNumber: Int
    ): List<ProviderStream> {
        if (animeId.isBlank() || episodeNumber < 1) return emptyList()

        return registry.all()
            .flatMap { provider ->
                runCatching {
                    provider.getStreams(animeId, episodeNumber)
                }.getOrDefault(emptyList())
            }
            .filter { it.url.isNotBlank() }
            .distinctBy { stream ->
                Triple(stream.providerId, stream.url, stream.quality)
            }
            .sortedWith(
                compareByDescending<ProviderStream> { qualityScore(it.quality) }
                    .thenBy { it.providerId }
            )
    }

    private suspend fun <T> forEachProvider(
        action: suspend (AnimeProvider) -> T?
    ): T? {
        for (provider in registry.all()) {
            val result = runCatching { action(provider) }.getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun buildKey(title: String, year: Int?): String =
        title.trim().lowercase().replace(Regex("\\s+"), " ") + "|" + (year ?: 0)

    private fun qualityScore(quality: String?): Int {
        val value = quality?.lowercase() ?: return 0
        return when {
            value.contains("1080") -> 1080
            value.contains("720") -> 720
            value.contains("480") -> 480
            value.contains("360") -> 360
            else -> 0
        }
    }
}
