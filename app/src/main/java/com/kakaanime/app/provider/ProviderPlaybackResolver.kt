package com.kakaanime.app.provider

/** Resolves a playable stream for an anime title/episode without exposing provider details to the UI. */
object ProviderPlaybackResolver {
    private val engine by lazy { ProviderFactory.createEngine() }

    suspend fun resolve(
        title: String,
        episodeNumber: Int,
        premium: Boolean
    ): NormalizedStream? {
        val candidates = engine.search(title)
        if (candidates.isEmpty()) return null

        // Try the best-ranked provider matches first. Each candidate keeps its
        // provider-specific id, so a failed source does not poison other sources.
        val ordered = candidates
            .sortedWith(compareBy<ProviderAnime> { it.providerId.isBlank() }.thenByDescending { it.latestEpisode ?: 0 })

        for (candidate in ordered) {
            val stream = runCatching {
                engine.getBestStream(
                    animeId = candidate.id,
                    episodeNumber = episodeNumber,
                    premium = premium
                )
            }.getOrNull()
            if (stream != null && stream.url.isNotBlank()) return stream
        }

        return null
    }
}
