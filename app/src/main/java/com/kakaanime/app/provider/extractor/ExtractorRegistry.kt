package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.extractor.extractors.GenericDirectExtractor
import com.kakaanime.app.provider.extractor.extractors.GenericEmbedExtractor

/**
 * Central registry for host-specific and generic stream extractors.
 *
 * Providers only need to discover server URLs. The registry decides which
 * extractor should receive each URL, keeping host logic reusable.
 * Generic direct/embed extractors are always available as the baseline;
 * provider-specific extractors can be supplied on top of them later.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList()
) {
    private val extractors = (
        extractors +
            GenericEmbedExtractor() +
            GenericDirectExtractor()
        )
        .distinctBy { it.id }
        .sortedByDescending { it.priority }

    fun find(url: String): List<StreamExtractor> =
        extractors.filter { extractor ->
            runCatching { extractor.canHandle(url) }.getOrDefault(false)
        }
}
