package com.kakaanime.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URI
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Dedicated Kuronime adapter with a gateway fallback for stream resolution. */
class KuronimeProvider : AnimeProvider {
    override val id = "kuronime"
    override val name = "Kuronime"
    override val priority = 80

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val base = "https://kuronime.sbs"
    private val sourceApi = "https://animeku.org/api/v9/sources"
    private val key = "3&!Z0M,VIZ;dZW==".toByteArray()
    private val gateway = RemoteSourceProvider(id, name, priority, "kura")

    override suspend fun search(query: String): List<ProviderAnime> = withContext(Dispatchers.IO) {
        runCatching {
            val current = currentBaseUrl()
            val request = Request.Builder()
                .url("$current/wp-admin/admin-ajax.php")
                .post("action=ajaxy_sf&sf_value=${enc(query)}&search=false".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .header("X-Requested-With", "XMLHttpRequest")
                .header("User-Agent", ua)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching emptyList()
                val root = JSONObject(response.body?.string().orEmpty())
                val all = root.optJSONArray("anime") ?: return@runCatching emptyList()
                buildList {
                    for (i in 0 until all.length()) {
                        val group = all.optJSONObject(i) ?: continue
                        val items = group.optJSONArray("all") ?: continue
                        for (j in 0 until items.length()) {
                            val item = items.optJSONObject(j) ?: continue
                            val title = item.optString("post_title").trim()
                            val url = item.optString("post_link").trim()
                            if (title.isNotBlank() && url.isNotBlank()) add(anime(url, title, item.optString("post_image")))
                        }
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun getAnime(animeId: String): ProviderAnime? = withContext(Dispatchers.IO) {
        runCatching {
            val url = animeId.removePrefix("$id:")
            val doc = Jsoup.parse(get(url), url)
            val title = doc.selectFirst(".entry-title")?.text()?.trim().orEmpty()
            if (title.isBlank()) return@runCatching null
            ProviderAnime(
                id = animeId,
                title = title,
                providerId = id,
                posterUrl = doc.selectFirst("div.l[itemprop=image] img, .l img")?.absUrl("src")?.ifBlank { null },
                description = doc.select("span.const > p").text().trim(),
                genres = doc.select(".infodetail li a").map { it.text().trim() }.filter { it.isNotBlank() }
            )
        }.getOrNull()
    }

    override suspend fun getEpisodes(animeId: String): List<ProviderEpisode> = withContext(Dispatchers.IO) {
        runCatching {
            val url = animeId.removePrefix("$id:")
            val doc = Jsoup.parse(get(url), url)
            doc.select("div.bixbox.bxcl > ul > li a").mapNotNull { link ->
                val href = link.absUrl("href").ifBlank { link.attr("href") }
                val text = link.text().trim()
                val number = Regex("(\\d+(?:[.,]\\d+)?)").find(text)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()?.toInt()
                if (href.isBlank() || number == null) null else ProviderEpisode(
                    id = "$id:${href}", animeId = animeId, number = number, providerId = id, title = text
                )
            }.distinctBy { it.number }.sortedByDescending { it.number }
        }.getOrDefault(emptyList())
    }

    override suspend fun getStreams(animeId: String, episodeNumber: Int): List<ProviderStream> = withContext(Dispatchers.IO) {
        val direct = runCatching {
            val episode = getEpisodes(animeId).firstOrNull { it.number == episodeNumber } ?: return@runCatching emptyList()
            val episodeUrl = episode.id.removePrefix("$id:")
            val doc = Jsoup.parse(get(episodeUrl), episodeUrl)
            val script = doc.select("script").map { it.data() }.firstOrNull { it.contains("_0xa100d42aa") }
                ?: return@runCatching emptyList()
            val encryptedId = script.substringAfter("_0xa100d42aa = \"").substringBefore("\";")
            if (encryptedId.isBlank()) return@runCatching emptyList()

            val request = Request.Builder()
                .url(sourceApi)
                .post(JSONObject(mapOf("id" to encryptedId)).toString().toRequestBody("application/json".toMediaType()))
                .header("User-Agent", ua)
                .header("Referer", "${origin(episodeUrl)}/")
                .build()
            val root = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching emptyList()
                JSONObject(response.body?.string().orEmpty())
            }

            val streams = mutableListOf<ProviderStream>()
            decrypt(root.optString("src"))?.let { json ->
                val src = runCatching { JSONObject(json).optString("src") }.getOrNull().orEmpty()
                if (src.startsWith("http", true)) streams += stream(src, "1080p")
            }
            decrypt(root.optString("mirror"))?.let { json ->
                val mirror = runCatching { JSONObject(json) }.getOrNull() ?: return@let
                val embeds = mirror.optJSONObject("embed") ?: return@let
                val keys = embeds.keys()
                while (keys.hasNext()) {
                    val server = keys.next()
                    val values = embeds.optJSONObject(server) ?: continue
                    val valueKeys = values.keys()
                    while (valueKeys.hasNext()) {
                        val value = values.optString(valueKeys.next())
                        if (value.startsWith("http", true)) streams += stream(value, server)
                    }
                }
            }
            ProviderStreamDeduplicator.deduplicate(streams)
        }.getOrDefault(emptyList())

        if (direct.isNotEmpty()) direct
        else gateway.getStreams(gatewayAnimeId(animeId), episodeNumber)
    }

    private fun anime(url: String, title: String, poster: String?) = ProviderAnime(
        id = "$id:$url", title = title, providerId = id, posterUrl = poster?.ifBlank { null }
    )

    private fun stream(url: String, quality: String) = ProviderStream(
        providerId = id, url = url, quality = quality,
        language = "Japanese", subtitleLanguage = "Indonesian",
        type = when {
            url.contains(".m3u8", true) -> StreamType.HLS
            url.contains(".mpd", true) -> StreamType.DASH
            url.contains(".mp4", true) -> StreamType.MP4
            else -> StreamType.UNKNOWN
        }
    )

    private fun decrypt(value: String): String? = runCatching {
        if (value.isBlank()) return null
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(ByteArray(16)))
        val decoded = Base64.getDecoder().decode(value)
        cipher.doFinal(decoded).toString(Charsets.UTF_8).trim().trim('"').replace("\\\"", "\"")
    }.getOrNull()

    private fun get(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", ua).build()
        return client.newCall(request).execute().use { it.body?.string().orEmpty() }
    }

    private fun gatewayAnimeId(animeId: String): String {
        val raw = animeId.removePrefix("$id:")
        val slug = runCatching { URI(raw).path.substringAfterLast('/').ifBlank { raw.trim('/') } }.getOrDefault(raw.trim('/'))
        return "$id:$slug"
    }

    private fun currentBaseUrl(): String = base
    private fun origin(url: String): String = URI(url).let { "${it.scheme}://${it.host}" }
    private fun enc(value: String) = java.net.URLEncoder.encode(value.trim(), "UTF-8")
    private val ua = "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36 KakaAnime/0.1"
}
