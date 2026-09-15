package com.kakaanime.app.provider

import android.util.Log

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

        Log.d(
            TAG,
            "STREAM_CCTV_ENGINE_STREAMS animeId=$animeId episode=$episodeNumber " +
                "raw=${rawStreams.size} dedup=${deduplicated.size} normalized=${normalized.size} " +
                "types=${normalized.groupingBy { it.type }.eachCount()} " +
                "qualities=${normalized.groupingBy { it.quality }.eachCount()}"
        )
        normalized.take(12).forEachIndexed { index, stream ->
            Log.d(
                TAG,
                "STREAM_CCTV_ENGINE_CANDIDATE[$index] provider=${stream.providerId} " +
                    "type=${stream.type} quality=${stream.quality} premium=${stream.isPremium} " +
                    "headers=${stream.headers.keys} url=${stream.url.take(180)}"
            )
        }

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
        val selected = StreamSelector.best(
            streams = result.streams,
            preferredQuality = preferredQuality,
            premium = premium
        )
        Log.d(
            TAG,
            "STREAM_CCTV_ENGINE_SELECTION animeId=$animeId episode=$episodeNumber " +
                "preferred=$preferredQuality premium=$premium selected=" +
                "${selected?.providerId ?: "NONE"} type=${selected?.type ?: "NONE"} " +
                "quality=${selected?.quality ?: "NONE"} url=${selected?.url?.take(180) ?: "none"}"
        )
        return selected
    }

    private companion object {
        const val TAG = "KakaAnime-StreamCCTV"
    }
}
