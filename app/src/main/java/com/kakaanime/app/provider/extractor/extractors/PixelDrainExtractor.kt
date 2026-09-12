package com.kakaanime.app.provider.extractor.extractors

import com.kakaanime.app.provider.ProviderStream
import com.kakaanime.app.provider.StreamType
import com.kakaanime.app.provider.extractor.StreamExtractor
import java.net.URI

/** PixelDrain resolver: /u/<id> -> stable API file URL. */
class PixelDrainExtractor : StreamExtractor {
    override val id = "pixeldrain"
    override val priority = 84

    override fun canHandle(url: String): Boolean =
        runCatching { URI(url).host.orEmpty().contains("pixeldrain.com", ignoreCase = true) }.getOrDefault(false)

    override suspend fun extract(url: String, referer: String?): List<ProviderStream> {
        val value = url.trim()
        val id = Regex("/u/([\\w-]+)", RegexOption.IGNORE_CASE)
            .find(value)?.groupValues?.getOrNull(1)
            ?: Regex("/api/file/([\\w-]+)", RegexOption.IGNORE_CASE)
                .find(value)?.groupValues?.getOrNull(1)
            ?: return emptyList()

        return listOf(
            ProviderStream(
                providerId = id,
                url = "https://pixeldrain.com/api/file/$id",
                type = StreamType.MP4,
                headers = buildMap {
                    referer?.takeIf { it.isNotBlank() }?.let { put("Referer", it) }
                }
            )
        )
    }
}
