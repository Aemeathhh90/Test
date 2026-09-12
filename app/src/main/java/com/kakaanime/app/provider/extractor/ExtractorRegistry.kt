package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.extractor.extractors.GenericDirectExtractor
import com.kakaanime.app.provider.extractor.extractors.GenericEmbedExtractor
import com.kakaanime.app.provider.extractor.extractors.JavascriptMediaExtractor
import com.kakaanime.app.provider.extractor.extractors.KrakenFilesExtractor
import com.kakaanime.app.provider.extractor.extractors.OtakudesuQrtzExtractor
import com.kakaanime.app.provider.extractor.extractors.OtakudesuServerExtractor
import com.kakaanime.app.provider.extractor.extractors.PixelDrainExtractor

/**
 * Central extractor registry.
 *
 * Ordering is specific-first, then JS/config parsing, generic embed traversal,
 * and finally direct-media handling. This keeps generic logic from masking a
 * host/provider-specific strategy while still giving resilient fallbacks.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList()
) {
    private val extractors = (
        (
            listOf(
                OtakudesuServerExtractor(),
                OtakudesuQrtzExtractor(),
                KrakenFilesExtractor(),
                PixelDrainExtractor(),
                JavascriptMediaExtractor()
            ) + extractors
        )
            .distinctBy { it.id }
            .sortedByDescending { it.priority } +
            GenericEmbedExtractor() +
            GenericDirectExtractor()
        )
        .distinctBy { it.id }

    fun find(url: String): List<StreamExtractor> =
        extractors.filter { extractor ->
            runCatching { extractor.canHandle(url) }.getOrDefault(false)
        }
}
