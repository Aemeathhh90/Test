package com.kakaanime.app.provider

/**
 * Removes duplicate stream sources before they reach the player.
 *
 * The same URL can be returned by multiple gateways/providers. We keep the
 * best candidate using provider priority, quality and header richness.
 */
object ProviderStreamDeduplicator {
    fun deduplicate(
        streams: List<ProviderStream>,
        providerPriorities: Map<String, Int> = emptyMap()
    ): List<ProviderStream> {
        return streams
            .filter { it.url.isNotBlank() }
            .groupBy { canonicalUrl(it.url) }
            .values
            .map { candidates ->
                candidates.maxWithOrNull(
                    compareBy<ProviderStream> { providerPriorities[it.providerId] ?: Int.MIN_VALUE }
                        .thenBy { qualityRank(it.quality) }
                        .thenBy { it.headers.size }
                ) ?: candidates.first()
            }
            .sortedWith(
                compareByDescending<ProviderStream> { qualityRank(it.quality) }
                    .thenByDescending { providerPriorities[it.providerId] ?: Int.MIN_VALUE }
                    .thenBy { it.providerId }
            )
    }

    private fun canonicalUrl(raw: String): String {
        return runCatching {
            val uri = java.net.URI(raw.trim())
            java.net.URI(
                uri.scheme?.lowercase(),
                uri.userInfo,
                uri.host?.lowercase(),
                uri.port,
                uri.path?.trimEnd('/'),
                uri.query,
                null
            ).toString()
        }.getOrElse { raw.trim().trimEnd('/') }
    }

    private fun qualityRank(quality: String?): Int {
        val value = quality.orEmpty().lowercase()
        return when {
            "2160" in value || "4k" in value -> 2160
            "1440" in value || "2k" in value -> 1440
            "1080" in value || "fhd" in value -> 1080
            "720" in value || "hd" in value -> 720
            "480" in value -> 480
            "360" in value -> 360
            else -> 0
        }
    }
}
