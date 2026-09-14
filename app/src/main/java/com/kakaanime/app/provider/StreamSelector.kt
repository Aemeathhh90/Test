package com.kakaanime.app.provider

object StreamSelector {

    fun best(
        streams: List<NormalizedStream>,
        preferredQuality: StreamQuality? = null,
        premium: Boolean = false
    ): NormalizedStream? {

        if (streams.isEmpty()) {
            return null
        }

        val available = streams.filter { stream ->
            stream.type != StreamType.UNKNOWN &&
                (!stream.isPremium || premium)
        }

        if (available.isEmpty()) {
            return null
        }

        if (preferredQuality != null) {
            val exact = available.firstOrNull { it.quality == preferredQuality }
            if (exact != null) {
                return exact
            }
        }

        return available.maxByOrNull { it.quality.value }
    }

    fun allAvailable(
        streams: List<NormalizedStream>,
        premium: Boolean = false
    ): List<NormalizedStream> {
        return streams
            .filter { it.type != StreamType.UNKNOWN && (premium || !it.isPremium) }
            .sortedByDescending { it.quality.value }
    }
}
