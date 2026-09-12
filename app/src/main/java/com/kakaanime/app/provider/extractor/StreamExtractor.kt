package com.kakaanime.app.provider.extractor

import com.kakaanime.app.provider.ProviderStream

/**
 * Resolves a provider/server URL into one or more playable streams.
 *
 * Extractors are intentionally independent from anime providers so the same
 * host resolver can be reused by Otakudesu, AnimeIndo, ANIMEIN, and others.
 */
interface StreamExtractor {
    val id: String
    val priority: Int

    /** Returns true when this extractor can handle the supplied URL. */
    fun canHandle(url: String): Boolean

    /** Resolves the URL into playable streams. */
    suspend fun extract(url: String, referer: String? = null): List<ProviderStream>
}
