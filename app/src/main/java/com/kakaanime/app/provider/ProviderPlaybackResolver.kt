package com.kakaanime.app.provider

/** Resolves playable streams and provider episode lists without exposing provider details to the UI. */
object ProviderPlaybackResolver {
    private val engine by lazy { ProviderFactory.createEngine() }

    suspend fun resolve(
        title: String,
        episodeNumber: Int,
        premium: Boolean,
        preferredQuality: StreamQuality? = null
    ): NormalizedStream? {
        val candidates = engine.search(title)
        if (candidates.isEmpty()) return null

        val ordered = candidates
            .sortedWith(compareBy<ProviderAnime> { it.providerId.isBlank() }.thenByDescending { it.latestEpisode ?: 0 })

        for (candidate in ordered) {
            val stream = runCatching {
                engine.getBestStream(
                    animeId = candidate.id,
                    episodeNumber = episodeNumber,
                    preferredQuality = preferredQuality,
                    premium = premium
                )
            }.getOrNull()
            if (stream != null && stream.url.isNotBlank()) return stream
        }

        return null
    }

    suspend fun episodes(title: String): List<ProviderEpisode> {
        val candidates = engine.search(title)
        if (candidates.isEmpty()) return emptyList()

        val ordered = candidates
            .sortedWith(compareBy<ProviderAnime> { it.providerId.isBlank() }.thenByDescending { it.latestEpisode ?: 0 })

        for (candidate in ordered) {
            val episodes = runCatching { engine.getEpisodes(candidate.id) }
                .getOrDefault(emptyList())
                .filter { it.number > 0 }
                .distinctBy { it.number }
                .sortedByDescending { it.number }
            if (episodes.isNotEmpty()) return episodes
        }

        return emptyList()
    }
}
