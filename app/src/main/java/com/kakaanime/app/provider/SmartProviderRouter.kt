package com.kakaanime.app.provider

class SmartProviderRouter(
    private val registry: ProviderRegistry,
    private val failureThreshold: Int = 2,
    private val cooldownMs: Long = 30_000L
) {
    private data class HealthState(
        var failures: Int = 0,
        var unavailableUntil: Long = 0L
    )

    private val health = mutableMapOf<String, HealthState>()

    suspend fun search(
        query: String
    ): List<ProviderAnime> {
        if (query.isBlank()) return emptyList()

        return eligibleProviders()
            .flatMap { provider ->
                runCatching { provider.search(query.trim()) }
                    .onSuccess { markSuccess(provider.id) }
                    .onFailure { markFailure(provider.id) }
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

        return eligibleProviders()
            .flatMap { provider ->
                runCatching {
                    provider.getStreams(animeId, episodeNumber)
                }
                    .onSuccess { markSuccess(provider.id) }
                    .onFailure { markFailure(provider.id) }
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
        action: suspend (AnimeProvider) -> T?
    ): T? {
        for (provider in eligibleProviders()) {
            val result = runCatching { action(provider) }
                .onSuccess { if (it != null) markSuccess(provider.id) }
                .onFailure { markFailure(provider.id) }
                .getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun eligibleProviders(): List<AnimeProvider> =
        registry.all().filter { isEligible(it.id) }

    private fun isEligible(providerId: String): Boolean {
        val state = health[providerId] ?: return true
        val now = System.currentTimeMillis()
        if (state.unavailableUntil <= now) {
            state.unavailableUntil = 0L
            state.failures = 0
            return true
        }
        return false
    }

    private fun markSuccess(providerId: String) {
        health.remove(providerId)
    }

    private fun markFailure(providerId: String) {
        val state = health.getOrPut(providerId) { HealthState() }
        state.failures++
        if (state.failures >= failureThreshold) {
            state.unavailableUntil = System.currentTimeMillis() + cooldownMs
        }
    }

    private fun providerPriority(providerId: String): Int =
        registry.get(providerId)?.priority ?: Int.MAX_VALUE

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
