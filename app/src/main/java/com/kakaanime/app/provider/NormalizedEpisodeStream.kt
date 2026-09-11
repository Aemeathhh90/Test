package com.kakaanime.app.provider

data class NormalizedEpisodeStream(
    val animeId: String,

    val episodeNumber: Int,

    val streams: List<NormalizedStream>
) {

    val bestStream: NormalizedStream?
        get() =
            StreamSelector.best(
                streams = streams
            )
}
