package com.kakaanime.app.provider

import com.kakaanime.app.provider.extractor.ExtractorRegistry
import com.kakaanime.app.provider.extractor.StreamResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class OtakudesuProvider : AnimeProvider {
    override val id = "otakudesu"
    override val name = "Otakudesu"
    override val priority = 10
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build()
    private val webSource = OtakudesuWebSource()
    private val streamResolver = StreamResolver(ExtractorRegistry())
    private val baseUrl = "https://qrtzanim.vercel.app/api"
    private val legacyUrl = "https://otakudesu-api-jade.vercel.app/api"

    override suspend fun search(query: String): List<ProviderAnime> {
        val normalized = query.trim(); if (normalized.isBlank()) return emptyList()
        val primary = requestJson("$baseUrl/search?q=${encode(normalized)}&page=1")?.optJSONObject("data")?.optJSONArray("results")?.toProviderAnimeList().orEmpty()
        if (primary.isNotEmpty()) return primary
        return requestJson("$legacyUrl/search/${encode(normalized)}")?.optJSONArray("search_results")?.toLegacyProviderAnimeList().orEmpty()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = normalizeAnimeSlug(animeId)
        val web = webSource.getAnime(slug)
        if (web != null) return ProviderAnime(id = "$id:$slug", title = web.title, providerId = id)
        val primary = requestJson("$baseUrl/anime/${encodePath(slug)}")?.optJSONObject("data")
        if (primary != null) return primary.toProviderAnime(slug)
        return requestJson("$legacyUrl/anime/${encodePath(slug)}")?.toLegacyProviderAnime(slug)
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = normalizeAnimeSlug(animeId)
        val web = webSource.getAnime(slug)
        if (web != null) {
            val episodes = webSource.getEpisodes(web)
            if (episodes.isNotEmpty()) return episodes.map { episode -> ProviderEpisode(id = "$id:${episode.url}", animeId = "$id:$slug", number = episode.number, providerId = id, title = episode.title, thumbnailUrl = episode.thumbnailUrl) }
        }
        val primary = requestJson("$baseUrl/anime/${encodePath(slug)}")?.optJSONObject("data")?.optJSONArray("episodeList")?.toProviderEpisodeList(slug).orEmpty()
        if (primary.isNotEmpty()) return primary
        return requestJson("$legacyUrl/anime/${encodePath(slug)}")?.let { it.optJSONObject("anime_detail") ?: it }?.optJSONArray("episode_list")?.toLegacyProviderEpisodeList(slug).orEmpty()
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val episode = getEpisodes(animeId).firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val episodeRef = episode.id.removePrefix("$id:")
        if (episodeRef.startsWith("http", ignoreCase = true)) {
            val resolved = streamResolver.resolve(urls = listOf(episodeRef), referer = null)
            if (resolved.isNotEmpty()) return resolved.map { it.copy(providerId = id) }
        }
        val episodeSlug = normalizeEpisodeSlug(episodeRef)
        val primary = requestJson("$baseUrl/episode/${encodePath(episodeSlug)}")
        if (primary != null) {
            val candidates = primary.optJSONObject("data")?.streamCandidates().orEmpty()
            val resolved = streamResolver.resolve(candidates, referer = episodeRef)
            if (resolved.isNotEmpty()) return resolved.map { it.copy(providerId = id) }
        }
        val legacy = requestJson("$legacyUrl/episode/${encodePath(episodeSlug)}")
        val streamUrl = legacy?.let { it.optJSONObject("episode_detail") ?: it }?.optString("stream_link")?.trim().orEmpty()
        if (streamUrl.isNotBlank()) {
            val resolved = streamResolver.resolve(listOf(streamUrl), referer = episodeRef)
            if (resolved.isNotEmpty()) return resolved.map { it.copy(providerId = id) }
        }
        return emptyList()
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).header("User-Agent", "KakaAnime/0.1").header("Accept", "application/json").build()
            client.newCall(request).execute().use { response -> if (!response.isSuccessful) null else response.body?.string()?.takeIf { it.isNotBlank() }?.let(::JSONObject) }
        }.getOrNull()
    }

    private fun JSONArray.toProviderAnimeList(): List<ProviderAnime> = buildList { for (i in 0 until length()) { val item = optJSONObject(i) ?: continue; val slug = normalizeAnimeSlug(item.optString("slug")); if (slug.isBlank()) continue; add(ProviderAnime(id = "$id:$slug", title = item.optString("title").ifBlank { "Unknown Anime" }, providerId = id, posterUrl = item.optString("thumbnail").ifBlank { null }, status = item.optString("status").ifBlank { "UNKNOWN" }, rating = item.optString("score").toDoubleOrNull())) } }
    private fun JSONArray.toLegacyProviderAnimeList(): List<ProviderAnime> = buildList { for (i in 0 until length()) { val item = optJSONObject(i) ?: continue; val slug = normalizeAnimeSlug(item.optString("endpoint")); if (slug.isBlank()) continue; add(ProviderAnime(id = "$id:$slug", title = item.optString("title").ifBlank { "Unknown Anime" }, providerId = id, posterUrl = item.optString("thumb").ifBlank { null }, status = item.optString("status").ifBlank { "UNKNOWN" }, rating = item.optString("rating").toDoubleOrNull())) } }
    private fun JSONObject.toProviderAnime(slug: String): ProviderAnime = ProviderAnime(id = "$id:$slug", title = optString("title").ifBlank { "Unknown Anime" }, providerId = id, posterUrl = optString("thumbnail").ifBlank { null }, description = optJSONArray("synopsis")?.toStringList()?.joinToString("\n") ?: optString("synopsis"), status = optJSONObject("metadata")?.optString("status").ifNullOrBlank("UNKNOWN"))
    private fun JSONObject.toLegacyProviderAnime(slug: String): ProviderAnime { val detail = optJSONObject("anime_detail") ?: this; return ProviderAnime(id = "$id:$slug", title = detail.optString("title").ifBlank { "Unknown Anime" }, providerId = id, posterUrl = detail.optString("thumb").ifBlank { null }, description = detail.optString("synopsis"), status = detail.optString("status").ifBlank { "UNKNOWN" }) }
    private fun JSONArray.toProviderEpisodeList(slug: String): List<ProviderEpisode> = buildList { for (i in 0 until length()) { val item = optJSONObject(i) ?: continue; val endpoint = item.optString("slug").trim('/'); val number = item.optString("episode").toIntOrNull() ?: extractEpisodeNumber(item.optString("title"), endpoint) ?: continue; val thumbnail = item.optString("thumbnail").ifBlank { item.optString("thumb").ifBlank { item.optString("image").ifBlank { null } } }; add(ProviderEpisode(id = "$id:${endpoint.ifBlank { "$slug-episode-$number-sub-indo" }}", animeId = "$id:$slug", number = number, providerId = id, title = item.optString("title").ifBlank { "Episode $number" }, thumbnailUrl = thumbnail)) } }.sortedBy { it.number }
    private fun JSONArray.toLegacyProviderEpisodeList(slug: String): List<ProviderEpisode> = buildList { for (i in 0 until length()) { val item = optJSONObject(i) ?: continue; val endpoint = item.optString("endpoint").trim('/'); val number = extractEpisodeNumber(item.optString("title"), endpoint) ?: continue; val thumbnail = item.optString("thumbnail").ifBlank { item.optString("thumb").ifBlank { item.optString("image").ifBlank { null } } }; add(ProviderEpisode(id = "$id:$endpoint", animeId = "$id:$slug", number = number, providerId = id, title = item.optString("title").ifBlank { "Episode $number" }, thumbnailUrl = thumbnail)) } }.sortedBy { it.number }
    private fun JSONObject.streamCandidates(): List<String> = buildList { val streams = optJSONArray("streams") ?: JSONArray(); for (i in 0 until streams.length()) { val item = streams.optJSONObject(i) ?: continue; item.optString("embedUrl").trim().takeIf { it.isNotBlank() }?.let(::add) }; optString("defaultStreamUrl").trim().takeIf { it.isNotBlank() }?.let(::add) }.distinct()
    private fun normalizeAnimeSlug(value: String): String { val raw = value.removePrefix("$id:").trim().trim('/'); val slug = raw.substringAfterLast("/anime/", raw).substringBefore("?").trim('/'); return when (slug.lowercase()) { "1piece-sub-indo", "onepiece-sub-indo" -> "one-piece-sub-indo"; else -> slug } }
    private fun normalizeEpisodeSlug(value: String): String = value.removePrefix("$id:").substringAfter("/episode/", value.removePrefix("$id:")).substringBefore("?").trim('/')
    private fun extractEpisodeNumber(title: String, slug: String): Int? = Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find("$title $slug")?.groupValues?.getOrNull(1)?.toIntOrNull()
    private fun JSONArray?.toStringList(): List<String> { if (this == null) return emptyList(); return buildList { for (i in 0 until length()) optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) } }
    private fun String?.ifNullOrBlank(fallback: String): String = if (this.isNullOrBlank()) fallback else this
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.trim('/').split('/').joinToString("/") { encode(it) }
}
