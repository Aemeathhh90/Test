package com.kakaanime.app.provider

class SmartProviderRouter(
    private val registry: ProviderRegistry
) {

    suspend fun search(
        query: String
    ): List<ProviderAnime> {

        val results = mutableListOf<ProviderAnime>()

        for (provider in registry.all()) {

            try {

                val found =
                    provider.search(query)

                results.addAll(found)

            } catch (_: Exception) {
                // Provider gagal dilewati.
                // Provider berikutnya tetap dicoba.
            }
        }

        return results
            .distinctBy {
                it.title.lowercase()
            }
    }

    suspend fun getAnime(
        animeId: String
    ): ProviderAnime? {

        for (provider in registry.all()) {

            try {

                val anime =
                    provider.getAnime(animeId)

                if (anime != null) {
                    return anime
                }

            } catch (_: Exception) {
                // Fallback otomatis ke provider berikutnya.
            }
        }

        return null
    }

    suspend fun getEpisodes(
        animeId: String
    ): List<ProviderEpisode> {

        for (provider in registry.all()) {

            try {

                val episodes =
                    provider.getEpisodes(animeId)

                if (episodes.isNotEmpty()) {
                    return episodes
                }

            } catch (_: Exception) {
                // Coba provider berikutnya.
            }
        }

        return emptyList()
    }

    suspend fun getStreams(
        animeId: String,
        episodeNumber: Int
    ): List<ProviderStream> {

        val candidates =
            mutableListOf<ProviderStream>()

        for (provider in registry.all()) {

            try {

                val streams =
                    provider.getStreams(
                        animeId = animeId,
                        episodeNumber = episodeNumber
                    )

                candidates.addAll(streams)

            } catch (_: Exception) {
                // Provider gagal.
                // Router otomatis lanjut ke provider berikutnya.
            }
        }

        return candidates
            .sortedWith(
                compareByDescending<ProviderStream> {
                    qualityScore(it.quality)
                }.thenBy {
                    it.providerId
                }
            )
    }

    private fun qualityScore(
        quality: String?
    ): Int {

        return when {
            quality == null -> 0

            quality.contains(
                "1080",
                ignoreCase = true
            ) -> 1080

            quality.contains(
                "720",
                ignoreCase = true
            ) -> 720

            quality.contains(
                "480",
                ignoreCase = true
            ) -> 480

            quality.contains(
                "360",
                ignoreCase = true
            ) -> 360

            else -> 0
        }
    }
}
