package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Source-specific AnimeSail adapter with conservative response parsing. */
class AnimeSailProvider : AnimeProvider {
    override val id = "animesail"
    override val name = "AnimeSail"
    override val priority = 25

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val bases = listOf("https://www.animesail.com", "https://animesail.com")

    override suspend fun search(query: String): List<ProviderAnime> {
        for (base in bases) {
            val root = requestJson("$base/?s=${encode(query)}") ?: continue
            val result = extractItems(root).mapNotNull { it.toAnime() }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:").trim('/')
        for (base in bases) {
            val root = requestJson("$base/anime/$slug") ?: requestJson("$base/$slug") ?: continue
            root.toAnime(slug)?.let { return it }
        }
        return null
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:").trim('/')
        val anime = getAnime("$id:$slug") ?: return emptyList()
        val root = requestJson(anime.id.removePrefix("$id:"))
        if (root != null) {
            val episodes = extractEpisodes(root).mapNotNull { item ->
                val number = item.episodeNumber() ?: return@mapNotNull null
                val endpoint = item.firstString("slug", "id", "url") ?: number.toString()
                ProviderEpisode("$id:$endpoint", "$id:$slug", number, id, item.firstString("title", "name") ?: "Episode $number")
            }
            if (episodes.isNotEmpty()) return episodes.distinctBy { it.number }.sortedBy { it.number }
        }
        return emptyList()
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val episodes = getEpisodes(animeId)
        val episode = episodes.firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val endpoint = episode.id.removePrefix("$id:")
        for (base in bases) {
            val root = requestJson("$base/episode/$endpoint") ?: requestJson("$base/$endpoint") ?: continue
            val streams = mutableListOf<ProviderStream>()
            collectStreams(root, streams)
            if (streams.isNotEmpty()) return streams.distinctBy { it.url }
        }
        return emptyList()
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).header("User-Agent", "KakaAnime/0.1").header("Accept", "application/json,text/html").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body?.string()?.trim()?.takeIf { it.startsWith("{") }?.let(::JSONObject)
            }
        }.getOrNull()
    }

    private fun extractItems(root: JSONObject): List<JSONObject> {
        for (key in listOf("results", "anime", "items", "data")) root.optJSONArray(key)?.let { return it.objects() }
        return if (root.has("title") || root.has("name")) listOf(root) else emptyList()
    }

    private fun extractEpisodes(root: JSONObject): List<JSONObject> {
        for (key in listOf("episodes", "episodeList", "episode_list")) root.optJSONArray(key)?.let { return it.objects() }
        return emptyList()
    }

    private fun JSONObject.toAnime(fallback: String): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle") ?: return null
        return ProviderAnime("$id:${firstString("slug", "id") ?: fallback}", title, id,
            posterUrl = firstString("poster", "posterUrl", "image", "thumbnail"),
            description = firstString("description", "synopsis") ?: "",
            genres = extractStrings("genres", "genre"),
            year = Regex("\\b(19\\d{2}|20\\d{2})\\b").find(firstString("year", "release", "released").orEmpty())?.value?.toIntOrNull(),
            status = firstString("status") ?: "UNKNOWN")
    }

    private fun collectStreams(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val keys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mp4", "source")
                val q = value.firstString("quality", "resolution") ?: quality
                keys.forEach { key -> value.optString(key).trim().takeIf { it.startsWith("http") }?.let { out += ProviderStream(id, it, q, "Japanese", "Indonesian") } }
                value.keys().asSequence().filterNot { it in keys }.forEach { collectStreams(value.opt(it), out, q) }
            }
            is JSONArray -> for (i in 0 until value.length()) collectStreams(value.opt(i), out, quality)
            is String -> if (value.startsWith("http")) out += ProviderStream(id, value, quality, "Japanese", "Indonesian")
        }
    }

    private fun JSONObject.firstString(vararg keys: String): String? = keys.firstNotNullOfOrNull { optString(it).trim().ifBlank { null } }
    private fun JSONObject.episodeNumber(): Int? = firstString("episode", "episodeNumber", "number")?.toIntOrNull() ?: Regex("(?:ep|episode)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(firstString("title", "name", "slug", "id").orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull()
    private fun JSONObject.extractStrings(vararg keys: String): List<String> = keys.firstNotNullOfOrNull { optJSONArray(it)?.objectsStrings() } ?: emptyList()
    private fun JSONArray.objectsStrings(): List<String> = buildList { for (i in 0 until length()) optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) }
    private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
    private fun encode(value: String) = URLEncoder.encode(value.trim(), "UTF-8")
}
