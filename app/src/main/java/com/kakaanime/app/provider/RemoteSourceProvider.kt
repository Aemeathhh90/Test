package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** Multi-gateway adapter. Sanka is a fallback, not the single point of failure. */
class RemoteSourceProvider(
    override val id: String,
    override val name: String,
    override val priority: Int,
    private val sourceSlug: String
) : AnimeProvider {
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    private val sanka = "https://www.sankavollerei.web.id/anime"
    private val wajik = "https://wajik-anime-api.vercel.app"
    private val kumanime = "https://kumanime.vercel.app/api"
    private val vharasc = "https://www.vharasc.my.id/api/v1"

    override suspend fun search(query: String): List<ProviderAnime> {
        for (url in searchUrls(query)) {
            val root = requestJson(url) ?: continue
            val result = extractItems(root).mapNotNull { it.toAnime() }
            if (result.isNotEmpty()) return result
        }
        return emptyList()
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? {
        val slug = animeId.removePrefix("$id:").trim('/')
        for (url in detailUrls(slug)) {
            val root = requestJson(url) ?: continue
            root.toAnime(slug)?.let { return it }
            extractItems(root).firstOrNull()?.toAnime(slug)?.let { return it }
        }
        return null
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> {
        val slug = animeId.removePrefix("$id:").trim('/')
        for (url in detailUrls(slug)) {
            val root = requestJson(url) ?: continue
            val episodes = extractEpisodeItems(root).mapNotNull { item ->
                val number = item.episodeNumber() ?: return@mapNotNull null
                val endpoint = item.firstString("slug", "endpoint", "id", "url") ?: number.toString()
                ProviderEpisode(id = "$id:$endpoint", animeId = "$id:$slug", number = number, providerId = id, title = item.firstString("title", "name") ?: "Episode $number", isNew = item.optBoolean("isNew", false))
            }.distinctBy { it.number }.sortedBy { it.number }
            if (episodes.isNotEmpty()) return episodes
        }
        return emptyList()
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> {
        val slug = animeId.removePrefix("$id:").trim('/')
        val episodeId = getEpisodes("$id:$slug").firstOrNull { it.number == episodeNumber }?.id?.removePrefix("$id:")?.trim('/') ?: episodeNumber.toString()
        for (url in episodeUrls(slug, episodeId, episodeNumber)) {
            val root = requestJson(url) ?: continue
            val streams = mutableListOf<ProviderStream>()
            collectStreams(root, streams)
            val playable = ProviderStreamDeduplicator.deduplicate(streams).filter { it.type != StreamType.UNKNOWN }
            if (playable.isNotEmpty()) return playable
        }
        return emptyList()
    }

    private fun searchUrls(query: String): List<String> = buildList {
        val q = encode(query)
        when (id) {
            "kuramanime", "oploverz" -> add("$wajik/$sourceSlug/search?q=$q")
            "animeindo" -> add("$kumanime/search/$q")
            "anoboy", "nimegami", "kusonime", "animekompi" -> add("$vharasc/$id/search/$q")
        }
        add("$sanka/$sourceSlug/search/$q")
    }

    private fun detailUrls(slug: String): List<String> = buildList {
        val value = encodePath(slug)
        when (id) {
            "kuramanime", "oploverz" -> add("$wajik/$sourceSlug/anime/$value")
            "animeindo" -> add("$kumanime/anime/$value")
            "anoboy", "nimegami", "kusonime", "animekompi" -> add("$vharasc/$id/detail/$value")
        }
        add("$sanka/$sourceSlug/detail/$value")
    }

    private fun episodeUrls(slug: String, episodeId: String, episodeNumber: Int): List<String> = buildList {
        val ep = encodePath(episodeId)
        when (id) {
            "kuramanime", "oploverz" -> { add("$wajik/$sourceSlug/episode/$ep"); add("$wajik/$sourceSlug/episode/$episodeNumber") }
            "animeindo" -> add("$kumanime/episode/$ep")
            "anoboy", "nimegami", "kusonime", "animekompi" -> add("$vharasc/$id/episode/$ep")
        }
        add("$sanka/$sourceSlug/episode/$ep")
    }

    private suspend fun requestJson(url: String): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36 KakaAnime/0.1").header("Accept", "application/json, text/plain, */*").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val body = response.body?.string()?.trim().orEmpty()
                if (body.isBlank()) return@runCatching null
                when { body.startsWith("{") -> JSONObject(body); body.startsWith("[") -> JSONObject().put("items", JSONArray(body)); else -> null }
            }
        }.getOrNull()
    }

    private fun extractItems(root: JSONObject): List<JSONObject> {
        val keys = listOf("results", "anime", "items", "search", "list", "animeList", "data", "result")
        for (key in keys) {
            root.optJSONArray(key)?.let { array -> array.objects().takeIf { it.isNotEmpty() }?.let { return it } }
            root.optJSONObject(key)?.let { nested -> extractItems(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        }
        return if (root.has("title") || root.has("name") || root.has("judul")) listOf(root) else emptyList()
    }

    private fun extractEpisodeItems(root: JSONObject): List<JSONObject> {
        for (key in listOf("episodes", "episode", "episodeList", "episode_list", "episodeListData", "data", "result")) {
            root.optJSONArray(key)?.let { array -> array.objects().takeIf { it.isNotEmpty() }?.let { return it } }
            root.optJSONObject(key)?.let { nested -> extractEpisodeItems(nested).takeIf { it.isNotEmpty() }?.let { return it } }
        }
        return emptyList()
    }

    private fun JSONObject.toAnime(fallbackId: String? = null): ProviderAnime? {
        val title = firstString("title", "name", "animeTitle", "judul") ?: return null
        val rawId = firstString("slug", "endpoint", "id", "animeId", "anime_id") ?: fallbackId ?: title.slugify()
        val episodes = extractEpisodeItems(this)
        return ProviderAnime(id = "$id:$rawId", title = title, providerId = id, alternativeTitles = extractStrings(this, "alternativeTitles", "alternatives"), posterUrl = firstString("poster", "posterUrl", "image", "thumbnail", "thumb", "poster_url"), backdropUrl = firstString("backdrop", "backdropUrl", "cover"), description = firstString("description", "synopsis", "sinopsis") ?: "", genres = extractStrings(this, "genres", "genre"), year = firstString("year", "released", "release")?.let { Regex("\\b(19\\d{2}|20\\d{2})\\b").find(it)?.value?.toIntOrNull() }, status = firstString("status") ?: "UNKNOWN", rating = firstString("rating", "score", "skor")?.toDoubleOrNull(), latestEpisode = episodes.mapNotNull { it.episodeNumber() }.maxOrNull())
    }

    private fun collectStreams(value: Any?, out: MutableList<ProviderStream>, quality: String? = null) {
        when (value) {
            is JSONObject -> {
                val directKeys = listOf("url", "file", "stream", "streamUrl", "stream_url", "m3u8", "mpd", "mp4", "source", "videoUrl", "link")
                val nextQuality = value.firstString("quality", "resolution", "res") ?: quality
                for (key in directKeys) value.optString(key).trim().takeIf { isPlayableUrl(it) }?.let { out += stream(it, nextQuality) }
                val keys = value.keys()
                while (keys.hasNext()) { val key = keys.next(); if (key !in directKeys) collectStreams(value.opt(key), out, nextQuality) }
            }
            is JSONArray -> for (i in 0 until value.length()) collectStreams(value.opt(i), out, quality)
            is String -> if (isPlayableUrl(value.trim())) out += stream(value.trim(), quality)
        }
    }

    private fun isPlayableUrl(url: String): Boolean { if (!url.startsWith("http", true)) return false; val value = url.lowercase(); return value.contains(".m3u8") || value.contains(".mpd") || value.contains(".mp4") }

    private fun stream(url: String, quality: String?): ProviderStream = ProviderStream(providerId = id, url = url, quality = quality?.ifBlank { null }, language = "Japanese", subtitleLanguage = "Indonesian", type = when { url.contains(".m3u8", true) -> StreamType.HLS; url.contains(".mpd", true) -> StreamType.DASH; else -> StreamType.MP4 })
    private fun JSONObject.firstString(vararg keys: String): String? = keys.firstNotNullOfOrNull { key -> optString(key).trim().ifBlank { null } }
    private fun JSONObject.episodeNumber(): Int? = firstString("episode", "episodeNumber", "number", "episodeNum")?.toIntOrNull() ?: Regex("(?:episode|eps|ep)[^0-9]*(\\d+)", RegexOption.IGNORE_CASE).find(firstString("title", "name", "slug", "endpoint", "id", "judul").orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull()
    private fun extractStrings(root: JSONObject, vararg keys: String): List<String> { for (key in keys) root.optJSONArray(key)?.let { array -> return buildList { for (i in 0 until array.length()) array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add) } }; return emptyList() }
    private fun JSONArray.objects(): List<JSONObject> = buildList { for (i in 0 until length()) optJSONObject(i)?.let(::add) }
    private fun String.slugify(): String = trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    private fun encode(value: String): String = URLEncoder.encode(value.trim(), "UTF-8")
    private fun encodePath(value: String): String = value.split('/').joinToString("/") { encode(it) }
}
