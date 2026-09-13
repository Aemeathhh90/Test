package com.kakaanime.app.network

import com.kakaanime.app.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Single mapping layer between the backend anime payload and the UI model.
 * The UI keeps a local fallback so a temporarily unavailable Codespace/backend
 * does not make Home blank during development.
 */
object AnimeRepository {

    suspend fun loadAnime(fallback: List<Anime>): List<Anime> = withContext(Dispatchers.IO) {
        val payload = ApiClient.getAnimeList() ?: return@withContext fallback
        parse(payload, fallback)
    }

    private fun parse(payload: JSONArray, fallback: List<Anime>): List<Anime> {
        return runCatching {
            buildList {
                for (index in 0 until payload.length()) {
                    val item = payload.optJSONObject(index) ?: continue
                    val title = item.optString("title").trim()
                    if (title.isEmpty()) continue

                    val episodes = item.optJSONArray("episodes")
                    val latestEpisode = (0 until (episodes?.length() ?: 0))
                        .mapNotNull { episodes?.optJSONObject(it)?.optInt("number") }
                        .maxOrNull() ?: 0

                    val genres = item.optJSONArray("genres")?.let { array ->
                        (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
                    }.orEmpty()

                    add(
                        Anime(
                            title = title,
                            latestEpisode = latestEpisode,
                            genre = genres.joinToString(", ").ifBlank { "Anime" },
                            description = item.optString("description"),
                            studio = item.optString("studio", "Unknown"),
                            season = item.optString("season", "Unknown"),
                            year = item.optString("year", "Unknown"),
                            type = item.optString("type", "TV"),
                            status = item.optString("status", "Ongoing"),
                            rating = item.optString("rating", "-"),
                            introStart = item.optLong("introStart", 0L),
                            introEnd = item.optLong("introEnd", 0L),
                            outroStart = item.optLong("outroStart", 0L),
                            outroEnd = item.optLong("outroEnd", 0L)
                        )
                    )
                }
            }.ifEmpty { fallback }
        }.getOrElse { fallback }
    }
}
