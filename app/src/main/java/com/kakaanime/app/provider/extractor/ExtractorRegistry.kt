package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.extractor.extractors.GenericDirectExtractor
import com.kakaanime.app.provider.extractor.extractors.GenericEmbedExtractor

/**
 * Extractor registry inspired by the proven specific-first/generic-last model.
 *
 * Host-specific extractors always get a chance before generic page/direct
 * extraction. Generic extractors are the final fallback instead of competing
 * with a specialized resolver.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList()
) {
    private val extractors = (
        extractors.distinctBy { it.id }.sortedByDescending { it.priority } +
            GenericEmbedExtractor() +
            GenericDirectExtractor()
        )
        .distinctBy { it.id }

    fun find(url: String): List<StreamExtractor> =
        extractors.filter { extractor ->
            runCatching { extractor.canHandle(url) }.getOrDefault(false)
        }
}
