package com.kakaanime.app.provider

/** Resolves playable streams and provider episode lists without exposing provider details to the UI. */
object ProviderPlaybackResolver {
    private val engine by lazy { ProviderFactory.createEngine() }

    suspend fun resolve(
        title: String,
        episodeNumber: Int,
        premium: Boolean,
        preferredQuality: StreamQuality? = null,
        seasonNumber: Int? = null,
        seasonTitle: String? = null,
    ): NormalizedStream? {
        val candidates = findCandidates(title, seasonNumber, seasonTitle)
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

    suspend fun episodes(
        title: String,
        seasonNumber: Int? = null,
        seasonTitle: String? = null,
    ): List<ProviderEpisode> {
        val candidates = findCandidates(title, seasonNumber, seasonTitle)
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

    private suspend fun findCandidates(
        title: String,
        seasonNumber: Int?,
        seasonTitle: String?,
    ): List<ProviderAnime> {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return emptyList()

        val queries = buildList {
            if (seasonNumber != null) {
                add("$normalizedTitle Season $seasonNumber")
                add("$normalizedTitle S$seasonNumber")
            }
            seasonTitle?.trim()?.takeIf { it.isNotBlank() }?.let { add("$normalizedTitle $it") }
            add(normalizedTitle)
        }.distinct()

        for (query in queries) {
            val results = runCatching { engine.search(query) }.getOrDefault(emptyList())
            if (results.isEmpty()) continue
            if (seasonNumber == null) return results

            val matching = results.filter { candidate ->
                val identity = SeasonIdentityParser.parse(candidate.title, candidate.id)
                identity.seasonNumber == seasonNumber || candidate.seasonNumber == seasonNumber
            }
            if (matching.isNotEmpty()) return matching
        }

        return emptyList()
    }
}
