package com.kakaanime.app.provider

data class NormalizedStream(
    val providerId: String,
    val url: String,

    val quality: StreamQuality = StreamQuality.UNKNOWN,

    val type: StreamType = StreamType.UNKNOWN,

    val language: String? = null,

    val subtitleLanguage: String? = null,

    val headers: Map<String, String> = emptyMap(),

    val isPremium: Boolean = false
)

enum class StreamQuality(
    val value: Int
) {
    Q360(360),
    Q480(480),
    Q720(720),
    Q1080(1080),
    UNKNOWN(0)
}
