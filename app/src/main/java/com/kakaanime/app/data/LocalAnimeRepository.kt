package com.kakaanime.app.data

class LocalAnimeRepository : AnimeRepository {

    private val anime = listOf(

        AnimeData(
            id = "one-piece",

            title = "One Piece",

            alternativeTitles = listOf(
                "OP",
                "One Piece"
            ),

            description =
                "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka mencari One Piece.",

            type = "TV",

            status = AnimeStatus.ONGOING,

            year = 1999,

            season = "Ongoing",

            genres = listOf(
                "Action",
                "Adventure",
                "Fantasy"
            ),

            studio = "Toei Animation",

            rating = 9.0,

            latestEpisode = 1140
        ),

        AnimeData(
            id = "solo-leveling",

            title = "Solo Leveling",

            alternativeTitles = listOf(
                "Ore dake Level Up na Ken"
            ),

            description =
                "Sung Jin-Woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.",

            type = "TV",

            status = AnimeStatus.FINISHED,

            year = 2025,

            season = "Season 2",

            genres = listOf(
                "Action",
                "Fantasy"
            ),

            studio = "A-1 Pictures",

            rating = 8.8,

            latestEpisode = 25
        )
    )

    override suspend fun searchAnime(
        query: String
    ): List<AnimeData> {

        if (query.isBlank()) {
            return anime
        }

        return anime.filter {
            it.title.contains(
                query,
                ignoreCase = true
            ) ||
            it.alternativeTitles.any { title ->
                title.contains(
                    query,
                    ignoreCase = true
                )
            }
        }
    }

    override suspend fun getAnime(
        animeId: String
    ): AnimeData? {

        return anime.firstOrNull {
            it.id == animeId
        }
    }

    override suspend fun getEpisodes(
        animeId: String
    ): List<EpisodeData> {

        val target = anime.firstOrNull {
            it.id == animeId
        }

        val latest = target?.latestEpisode ?: return emptyList()

        return (latest downTo maxOf(1, latest - 49))
            .map { number ->

                EpisodeData(
                    id = "$animeId-$number",

                    animeId = animeId,

                    episodeNumber = number,

                    title = "Episode $number",

                    isNew = number == latest
                )
            }
    }

    override suspend fun getLatestUpdates(): List<AnimeData> {

        return anime.sortedByDescending {
            it.latestEpisode ?: 0
        }
    }
}
