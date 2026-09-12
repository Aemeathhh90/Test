package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.extractor.extractors.GenericDirectExtractor
import com.kakaanime.app.provider.extractor.extractors.GenericEmbedExtractor
import com.kakaanime.app.provider.extractor.extractors.JavascriptMediaExtractor
import com.kakaanime.app.provider.extractor.extractors.KrakenFilesExtractor
import com.kakaanime.app.provider.extractor.extractors.OtakudesuServerExtractor
import com.kakaanime.app.provider.extractor.extractors.PixelDrainExtractor
import com.kakaanime.app.provider.extractor.extractors.SamehadakuEpisodeExtractor

/**
 * Central extractor registry.
 *
 * Ordering is specific-first, then JS/config parsing, generic embed traversal,
 * and finally direct-media handling. This keeps generic logic from masking a
 * host/provider-specific strategy while still giving resilient fallbacks.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList(),
    includeSamehadakuEpisodeExtractor: Boolean = true
) {
    private val extractors = buildList {
        if (includeSamehadakuEpisodeExtractor) {
            // Important: construct this only when enabled. Using
            // SamehadakuEpisodeExtractor().takeIf { include... } would still
            // invoke its constructor when disabled and recursively construct
            // another registry.
            add(SamehadakuEpisodeExtractor())
        }

        addAll(
            listOf(
                OtakudesuServerExtractor(),
                KrakenFilesExtractor(),
                PixelDrainExtractor(),
                JavascriptMediaExtractor()
            )
        )
        addAll(extractors)

        val specific = distinctBy { it.id }
            .sortedByDescending { it.priority }

        clear()
        addAll(specific)
        add(GenericEmbedExtractor())
        add(GenericDirectExtractor())
    }.distinctBy { it.id }

    fun find(url: String): List<StreamExtractor> =
        extractors.filter { extractor ->
            runCatching { extractor.canHandle(url) }.getOrDefault(false)
        }
}
