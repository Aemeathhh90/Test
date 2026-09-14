package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Resolves provider/server URLs through the extractor chain.
 *
 * Only typed, validated streams are allowed to leave this resolver. A URL
 * being discovered is not enough to make it playable by Media3.
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

        if (inputUrls.isEmpty()) return@supervisorScope emptyList()

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
            .filter { it.type != StreamType.UNKNOWN }
            .distinctBy { it.url }

        if (validated.isNotEmpty()) return@supervisorScope validated

        // OkHttp may only see an iframe/config page while the real media URL is
        // created by JavaScript. Give WebView a chance, but never return the
        // unvalidated extractor output to Media3.
        val browserInputs = (extracted.map { it.url } + inputUrls)
            .distinct()
        resolveWithBrowser(browserInputs, referer)
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
            .filter { it.url.startsWith("http", ignoreCase = true) }
            .distinctBy { it.url }
    }
}
