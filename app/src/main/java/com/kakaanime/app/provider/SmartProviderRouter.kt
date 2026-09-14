package com.kakaanime.app.provider

class SmartProviderRouter(
    private val registry: ProviderRegistry,
    private val failureThreshold: Int = 2,
    private val cooldownMs: Long = 30_000L
) {
    private enum class Operation { SEARCH, ANIME, EPISODES, STREAMS }

    private data class HealthState(
        var failures: Int = 0,
        var unavailableUntil: Long = 0L
    )

    private val health = mutableMapOf<Pair<String, Operation>, HealthState>()

    suspend fun search(
        query: String
    ): List<ProviderAnime> {
        if (query.isBlank()) return emptyList()

        return eligibleProviders(Operation.SEARCH)
            .flatMap { provider ->
                runCatching { provider.search(query.trim()) }
                    .onSuccess { markSuccess(provider.id, Operation.SEARCH) }
                    .onFailure { markFailure(provider.id, Operation.SEARCH) }
                    .getOrDefault(emptyList())
                    .map { it.copy(providerId = provider.id) }
            }
            .distinctBy { anime ->
                buildKey(anime.title, anime.year, anime.seasonNumber, anime.seasonTitle, anime.animeGroupId)
            }
    }

    suspend fun getAnime(
        animeId: String
    ): ProviderAnime? {
        if (animeId.isBlank()) return null

        return forEachProvider(Operation.ANIME) { provider ->
            provider.getAnime(animeId)?.copy(providerId = provider.id)
        }
    }

    suspend fun getEpisodes(
        animeId: String
    ): List<ProviderEpisode> {
        if (animeId.isBlank()) return emptyList()

        return forEachProvider(Operation.EPISODES) { provider ->
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

        return eligibleProviders(Operation.STREAMS)
            .flatMap { provider ->
                runCatching {
                    provider.getStreams(animeId, episodeNumber)
                }
                    .onSuccess { markSuccess(provider.id, Operation.STREAMS) }
                    .onFailure { markFailure(provider.id, Operation.STREAMS) }
                    .getOrDefault(emptyList())
                    .map { it.copy(providerId = provider.id) }
            }
            .filter { it.url.isNotBlank() }
            .distinctBy { stream ->
                Triple(stream.providerId, stream.url, stream.quality)
            }
            .sortedWith(
                compareByDescending<ProviderStream> { qualityScore(it.quality) }
                    .thenBy { providerPriority(it.providerId) }
                    .thenBy { it.providerId }
            )
    }

    private suspend fun <T> forEachProvider(
        operation: Operation,
        action: suspend (AnimeProvider) -> T?
    ): T? {
        for (provider in eligibleProviders(operation)) {
            val result = runCatching { action(provider) }
                .onSuccess { if (it != null) markSuccess(provider.id, operation) }
                .onFailure { markFailure(provider.id, operation) }
                .getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun eligibleProviders(operation: Operation): List<AnimeProvider> =
        registry.all().filter { isEligible(it.id, operation) }

    private fun isEligible(providerId: String, operation: Operation): Boolean {
        val key = providerId to operation
        val state = health[key] ?: return true
        val now = System.currentTimeMillis()
        if (state.unavailableUntil <= now) {
            health.remove(key)
            return true
        }
        return false
    }

    private fun markSuccess(providerId: String, operation: Operation) {
        health.remove(providerId to operation)
    }

    private fun markFailure(providerId: String, operation: Operation) {
        val key = providerId to operation
        val state = health.getOrPut(key) { HealthState() }
        state.failures++
        if (state.failures >= failureThreshold) {
            state.unavailableUntil = System.currentTimeMillis() + cooldownMs
        }
    }

    private fun providerPriority(providerId: String): Int =
        registry.get(providerId)?.priority ?: Int.MAX_VALUE

    private fun buildKey(
        title: String,
        year: Int?,
        seasonNumber: Int?,
        seasonTitle: String?,
        animeGroupId: String,
    ): String = buildString {
        append(animeGroupId.trim().lowercase().ifBlank { title.trim().lowercase() })
        append("|")
        append(title.trim().lowercase().replace(Regex("\\s+"), " "))
        append("|")
        append(year ?: 0)
        append("|")
        append(seasonNumber ?: "na")
        append("|")
        append(seasonTitle?.trim()?.lowercase()?.replace(Regex("\\s+"), " ").orEmpty())
    }

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
