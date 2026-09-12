package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.StreamExtractor

/**
 * Handles URLs that are already direct media manifests/files.
 * Host-specific extractors can be registered later without changing this one.
 */
class GenericDirectExtractor : StreamExtractor {
    override val id = "generic-direct"
    override val priority = 0

    override fun canHandle(url: String): Boolean =
        url.isDirectMediaUrl()

    override suspend fun extract(
        url: String,
        referer: String?
    ): List<ProviderStream> {
        if (!canHandle(url)) return emptyList()

        return listOf(
            ProviderStream(
                providerId = "resolver",
                url = url,
                type = url.toStreamType(),
                headers = referer?.let { mapOf("Referer" to it) }.orEmpty()
            )
        )
    }

    private fun String.isDirectMediaUrl(): Boolean {
        val clean = substringBefore('?').lowercase()
        return clean.endsWith(".m3u8") ||
            clean.endsWith(".mpd") ||
            clean.endsWith(".mp4") ||
            clean.endsWith(".mkv") ||
            clean.endsWith(".webm")
    }

    private fun String.toStreamType(): StreamType {
        val clean = substringBefore('?').lowercase()
        return when {
            clean.endsWith(".m3u8") -> StreamType.HLS
            clean.endsWith(".mpd") -> StreamType.DASH
            clean.endsWith(".mp4") ||
                clean.endsWith(".mkv") ||
                clean.endsWith(".webm") -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    }
}
