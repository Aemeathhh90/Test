package com.kakaanime.app.provider.extractor

/**
 * Central registry for host-specific and generic stream extractors.
 *
 * Providers only need to discover server URLs. The registry decides which
 * extractor should receive each URL, keeping host logic reusable.
 */
class ExtractorRegistry(
    extractors: List<StreamExtractor> = emptyList()
) {
    private val extractors = extractors
        .distinctBy { it.id }
        .sortedByDescending { it.priority }

    fun find(url: String): List<StreamExtractor> =
        extractors.filter { extractor ->
            runCatching { extractor.canHandle(url) }.getOrDefault(false)
        }
}
