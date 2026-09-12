package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Resolves provider/server URLs through the extractor chain.
 *
 * The resolver follows the more defensive pipeline used by mature stream
 * clients: dispatch to specific and generic extractors, collect independent
 * candidates, then validate the candidates before handing them to Media3.
 */
class StreamResolver(
    private val registry: ExtractorRegistry,
    private val validator: StreamValidator = StreamValidator()
) {
    suspend fun resolve(
        urls: List<String>,
        referer: String? = null
    ): List<ProviderStream> = supervisorScope {
        val extracted = urls.map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .flatMap { url ->
                val candidates = registry.find(url)
                candidates.map { extractor ->
                    async {
                        runCatching {
                            extractor.extract(url, referer)
                        }.getOrDefault(emptyList())
                    }
                }.awaitAll().flatten()
            }
            .filter { it.url.startsWith("http", ignoreCase = true) }
            .distinctBy { it.url }

        if (extracted.isEmpty()) return@supervisorScope emptyList()

        val validated = extracted.map { stream ->
            async { validator.validate(stream) }
        }.awaitAll()
            .filterNotNull()
            .distinctBy { it.url }

        // Validation is a reliability signal, not a hard dependency. Some
        // signed/CDN URLs reject lightweight probes but still work in Media3.
        if (validated.isNotEmpty()) validated else extracted
    }
}
