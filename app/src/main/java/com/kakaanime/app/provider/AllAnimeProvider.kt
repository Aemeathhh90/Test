package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Dedicated AllAnime adapter backed by the documented community API. */
class AllAnimeProvider : AnimeProvider {
    override val id = "allanime"
    override val name = "AllAnime"
    override val priority = 320

    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    private val base = "https://allanime-api.mdtahseen7378.workers.dev"

    override suspend fun search(query: String): List<ProviderAnime> {
        val root = request("/search?query=${encode(query)}") ?: return emptyList()
        return array(root).mapNotNull { item ->
            val showId = item.optString("id").trim().ifBlank { return@mapNotNull null }
            val title = item.optString("title").trim().ifBlank { return@mapNotNull null }
            ProviderAnime(id = "$id:$showId", title = title, providerId = id, latestEpisode = maxOf(item.optInt("episodes_sub", 0), item.optInt("episodes_dub", 0)).takeIf { it > 0 })
        }
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val showId = animeId.substringAfter(":", animeId)
        val root = request("/anime/${encodePath(showId)}") ?: return null
        val data = payload(root)
        val title = firstString(data, "title", "name") ?: return null
        return ProviderAnime(id = "$id:$showId", title = title, providerId = id, description = firstString(data, "description", "synopsis") ?: "", posterUrl = firstString(data, "poster", "posterUrl", "image", "thumbnail"), backdropUrl = firstString(data, "backdrop", "cover"), genres = stringArray(data, "genres", "genre"), alternativeTitles = stringArray(data, "alternativeTitles", "alternatives"), year = firstString(data, "year", "release", "released")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() }, status = firstString(data, "status") ?: "UNKNOWN")
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val showId = animeId.substringAfter(":", animeId)
        val root = request("/episodes/${encodePath(showId)}?mode=sub") ?: return emptyList()
        return array(root).mapNotNull { item ->
            val number = when { item.has("episode") -> item.optInt("episode", 0); item.has("number") -> item.optInt("number", 0); else -> item.optString("episodeNumber").toIntOrNull() ?: 0 }
            if (number <= 0) return@mapNotNull null
            ProviderEpisode(id = "$id:$showId:$number", animeId = "$id:$showId", number = number, providerId = id, title = item.optString("title").trim().ifBlank { "Episode $number" })
        }.distinctBy { it.number }.sortedBy { it.number }
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val showId = animeId.substringAfter(":", animeId)
        val streams = mutableListOf<ProviderStream>()
        for (quality in listOf("best", "1080p", "720p", "480p")) {
            val root = request("/episode_url?show_id=${encode(showId)}&ep_no=$episodeNumber&mode=sub&quality=$quality") ?: continue
            collectUrls(root, quality, streams)
        }
        return ProviderStreamDeduplicator.deduplicate(streams)
    }

    private suspend fun request(path: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(base + path).header("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36 KakaAnime/0.1").header("Accept", "application/json, text/plain, */*").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string()?.trim().orEmpty()
                if (body.isBlank()) null else when { body.startsWith("[") -> JSONObject().put("items", JSONArray(body)); body.startsWith("{") -> JSONObject(body); else -> null }
            }
        }.getOrNull()
    }

    private fun payload(root: JSONObject): JSONObject {
        if (root.has("title") || root.has("name") || root.has("id")) return root
        for (key in listOf("result", "data")) root.optJSONObject(key)?.let { return payload(it) }
        return root
    }

    private fun array(root: JSONObject): List<JSONObject> {
        for (key in listOf("results", "items", "episodes")) root.optJSONArray(key)?.let { return it.objects() }
        for (key in listOf("result", "data")) {
            root.optJSONArray(key)?.let { return it.objects() }
            root.optJSONObject(key)?.let { nested -> array(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        }
        return if (root.has("title") || root.has("name") || root.has("episode") || root.has("number")) listOf(root) else emptyList()
    }

    private fun collectUrls(value: Any?, quality: String, out: MutableList<ProviderStream>) {
        when (value) {
            is JSONObject -> { val keys = value.keys(); while (keys.hasNext()) { val key = keys.next(); val item = value.opt(key); if (item is String && item.startsWith("http", true)) out += stream(item, quality) else collectUrls(item, quality, out) } }
            is JSONArray -> for (i in 0 until value.length()) collectUrls(value.opt(i), quality, out)
            is String -> if (value.startsWith("http", true)) out += stream(value, quality)
        }
    }

    private fun stream(url: String, quality: String) = ProviderStream(providerId = id, url = url, quality = quality, language = "Japanese", subtitleLanguage = "Indonesian", type = when { url.contains(".m3u8", true) -> StreamType.HLS; url.contains(".mpd", true) -> StreamType.DASH; url.contains(".mp4", true) -> StreamType.MP4; else -> StreamType.UNKNOWN })
    private fun firstString(root: JSONObject, vararg keys: String): String? = keys.firstNotNullOfOrNull { root.optString(it).trim().ifBlank { null } }
    private fun stringArray(root: JSONObject, vararg keys: String): List<String> { for (key in keys) root.optJSONArray(key)?.let { return it.strings() }; return emptyList() }
    private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
    private fun JSONArray.strings(): List<String> = buildList { for (i in 0 until length()) optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) }
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = encode(value).replace("+", "%20")
}
