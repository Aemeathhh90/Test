package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream

/**
 * Resolves server URLs through the registered extractor chain.
 *
 * This is the compatibility boundary between provider discovery and Media3:
 * providers return server/embed URLs, while this class is responsible for
 * turning them into playable ProviderStream values.
 */
class StreamResolver(
    private val registry: ExtractorRegistry
) {
    suspend fun resolve(
        urls: List<String>,
        referer: String? = null
    ): List<ProviderStream> {
        val results = mutableListOf<ProviderStream>()

        for (url in urls.map(String::trim).filter(String::isNotBlank).distinct()) {
            val candidates = registry.find(url)
            for (extractor in candidates) {
                val extracted = runCatching {
                    extractor.extract(url, referer)
                }.getOrDefault(emptyList())
                if (extracted.isNotEmpty()) {
                    results += extracted
                    break
                }
            }
        }

        return results.distinctBy { it.url }
    }
}
