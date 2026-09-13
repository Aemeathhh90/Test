package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Resolves provider/server URLs through the extractor chain.
 *
 * The resolver dispatches specific and generic extractors, validates the
 * resulting candidates, then uses a browser-backed fallback when a player
 * page is JavaScript-driven and no typed stream was recovered.
 */
class StreamResolver(
    private val registry: ExtractorRegistry,
    private val validator: StreamValidator = StreamValidator()
) {
    suspend fun resolve(
        urls: List<String>,
        referer: String? = null
    ): List<ProviderStream> = supervisorScope {
        val inputUrls = urls.map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

        val extracted = inputUrls.flatMap { url ->
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

        if (extracted.isEmpty()) {
            return@supervisorScope resolveWithBrowser(inputUrls, referer)
        }

        val validated = extracted.map { stream ->
            async { validator.validate(stream) }
        }.awaitAll()
            .filterNotNull()
            .distinctBy { it.url }

        val typedValidated = validated.filter { it.type != StreamType.UNKNOWN }
        if (typedValidated.isNotEmpty()) return@supervisorScope typedValidated

        // JavaScript player pages can expose only an iframe/config URL to
        // OkHttp. Try browser resolution against both the original page and
        // every extracted host/player URL. This preserves the extractor chain
        // instead of relying on the episode page alone.
        val browserInputs = (extracted.map { it.url } + inputUrls)
            .distinct()
        val browserStreams = resolveWithBrowser(browserInputs, referer)
        if (browserStreams.isNotEmpty()) return@supervisorScope browserStreams

        // Validation remains a reliability signal rather than a hard dependency.
        // UNKNOWN validated streams should never survive: the validator now
        // rejects UNKNOWN unless a supported media type was actually proven.
        if (validated.isNotEmpty()) validated else extracted
    }

    private suspend fun resolveWithBrowser(
        urls: List<String>,
        referer: String?
    ): List<ProviderStream> = supervisorScope {
        urls.map { url ->
            async {
                runCatching {
                    BrowserMediaResolver().resolve(url, referer)
                }.getOrDefault(emptyList())
            }
        }.awaitAll()
            .flatten()
            .filter { it.type != StreamType.UNKNOWN }
            .distinctBy { it.url }
    }
}
