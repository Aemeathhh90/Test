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

        println("STREAM_CCTV_INPUT count=${inputUrls.size} referer=${referer?.take(180)}")
        inputUrls.forEachIndexed { index, url ->
            val candidates = registry.find(url)
            println(
                "STREAM_CCTV_ROUTE[$index] url=${url.take(240)} " +
                    "extractors=${candidates.joinToString(",") { it.id }}"
            )
        }

        if (inputUrls.isEmpty()) {
            println("STREAM_CCTV_STOP reason=empty_input")
            return@supervisorScope emptyList()
        }

        val extracted = inputUrls.flatMap { url ->
            val candidates = registry.find(url)
            candidates.map { extractor ->
                async {
                    val result = runCatching {
                        extractor.extract(url, referer)
                    }.onFailure {
                        println(
                            "STREAM_CCTV_EXTRACTOR_ERROR id=${extractor.id} " +
                                "url=${url.take(180)} " +
                                "error=${it::class.java.simpleName}:${it.message}"
                        )
                    }.getOrDefault(emptyList())
                    println(
                        "STREAM_CCTV_EXTRACTOR id=${extractor.id} " +
                            "input=${url.take(180)} outputs=${result.size} " +
                            "types=${result.joinToString(",") { it.type.name }}"
                    )
                    result
                }
            }.awaitAll().flatten()
        }
            .filter { it.url.startsWith("http", ignoreCase = true) }
            .distinctBy { it.url }

        println("STREAM_CCTV_EXTRACTED count=${extracted.size}")
        extracted.forEachIndexed { index, stream ->
            println(
                "STREAM_CCTV_CANDIDATE[$index] type=${stream.type} " +
                    "url=${stream.url.take(240)} headers=${stream.headers.keys}"
            )
        }

        if (extracted.isEmpty()) {
            println("STREAM_CCTV_STAGE direct_validation input_count=${inputUrls.size}")
            val direct = validateDirectUrls(inputUrls)
            println("STREAM_CCTV_DIRECT_RESULT count=${direct.size}")
            if (direct.isNotEmpty()) return@supervisorScope direct
            println("STREAM_CCTV_STAGE browser_fallback input_count=${inputUrls.size}")
            val browser = resolveWithBrowser(inputUrls, referer)
            println("STREAM_CCTV_BROWSER_RESULT count=${browser.size}")
            return@supervisorScope browser
        }

        val validated = extracted.map { stream ->
            async {
                val result = validator.validate(stream)
                println(
                    "STREAM_CCTV_VALIDATE url=${stream.url.take(180)} " +
                        "inputType=${stream.type} resultType=${result?.type}"
                )
                result
            }
        }.awaitAll()
            .filterNotNull()
            .filter { it.type != StreamType.UNKNOWN }
            .distinctBy { it.url }

        println("STREAM_CCTV_VALIDATED count=${validated.size}")
        if (validated.isNotEmpty()) return@supervisorScope validated

        println("STREAM_CCTV_STAGE final_direct_validation input_count=${inputUrls.size}")
        val direct = validateDirectUrls(inputUrls)
        println("STREAM_CCTV_FINAL_DIRECT_RESULT count=${direct.size}")
        if (direct.isNotEmpty()) return@supervisorScope direct

        val browserInputs = (extracted.map { it.url } + inputUrls).distinct()
        println("STREAM_CCTV_STAGE browser_fallback input_count=${browserInputs.size}")
        val browser = resolveWithBrowser(browserInputs, referer)
        println("STREAM_CCTV_BROWSER_RESULT count=${browser.size}")
        browser
    }

    private suspend fun validateDirectUrls(urls: List<String>): List<ProviderStream> = supervisorScope {
        urls.map { url ->
            async {
                val result = validator.validate(
                    ProviderStream(
                        providerId = "",
                        url = url,
                        type = StreamType.UNKNOWN
                    )
                )
                println(
                    "STREAM_CCTV_DIRECT_VALIDATE url=${url.take(180)} resultType=${result?.type}"
                )
                result
            }
        }.awaitAll()
            .filterNotNull()
            .filter { it.type != StreamType.UNKNOWN }
            .distinctBy { it.url }
    }

    private suspend fun resolveWithBrowser(
        urls: List<String>,
        referer: String?
    ): List<ProviderStream> = supervisorScope {
        urls.map { url ->
            async {
                val result = runCatching {
                    BrowserMediaResolver().resolve(url, referer)
                }.onFailure {
                    println(
                        "STREAM_CCTV_BROWSER_ERROR url=${url.take(180)} " +
                            "error=${it::class.java.simpleName}:${it.message}"
                    )
                }.getOrDefault(emptyList())
                println(
                    "STREAM_CCTV_BROWSER url=${url.take(180)} outputs=${result.size} " +
                        "types=${result.joinToString(",") { it.type.name }}"
                )
                result
            }
        }.awaitAll()
            .flatten()
            .filter { it.type != StreamType.UNKNOWN }
            .filter { it.url.startsWith("http", ignoreCase = true) }
            .distinctBy { it.url }
    }
}
