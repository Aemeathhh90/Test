package com.kakaanime.app.data

interface AnimeRepository {

    suspend fun searchAnime(
        query: String
    ): List<AnimeData>

    suspend fun getAnime(
        animeId: String
    ): AnimeData?

    suspend fun getEpisodes(
        animeId: String
    ): List<EpisodeData>

    suspend fun getLatestUpdates(): List<AnimeData>
}
