package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.extractor.extractors.GenericDirectExtractor
import com.kakaanime.app.provider.extractor.extractors.GenericEmbedExtractor
import com.kakaanime.app.provider.extractor.extractors.KrakenFilesExtractor
import com.kakaanime.app.provider.extractor.extractors.OtakudesuServerExtractor
import com.kakaanime.app.provider.extractor.extractors.PixelDrainExtractor

/**
 * Central extractor registry.
 *
 * Ordering follows the proven specific-first/generic-last model:
 * provider/server resolver -> host-specific resolver -> generic embed -> direct.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList()
) {
    private val extractors = (
        (
            listOf(
                OtakudesuServerExtractor(),
                KrakenFilesExtractor(),
                PixelDrainExtractor()
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
