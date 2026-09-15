package com.kakaanime.app

import androidx.compose.runtime.*
import com.kakaanime.app.data.AniListCalendarService
import com.kakaanime.app.data.AniListScheduleEntry
import com.kakaanime.app.ui.calendar.CalendarDayUi
import com.kakaanime.app.ui.calendar.CalendarEpisodeUi
import com.kakaanime.app.ui.calendar.CalendarUiState
import com.kakaanime.app.ui.calendar.CalendarV1Screen
import com.kakaanime.app.ui.home.HomeAnimeUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Compatibility entry point. Schedule/data loading stays here; Calendar V1 owns presentation. */
@Composable
fun CalendarScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    favoriteTitles: Set<String> = emptySet(),
) {
    val service = remember { AniListCalendarService() }
    val today = remember { LocalDate.now() }
    val days = remember(today) { (-7L..30L).map { today.plusDays(it) } }
    var schedules by remember { mutableStateOf<List<AniListScheduleEntry>>(emptyList()) }

    LaunchedEffect(today) {
        schedules = service.getSchedule(
            from = today.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
            days = 37,
        )
    }

    fun resolveAnime(entry: AniListScheduleEntry): Anime =
        animeList.firstOrNull { it.matchesCalendarEntry(entry) } ?: entry.toCalendarAnime()

    fun toUi(entry: AniListScheduleEntry): CalendarEpisodeUi {
        val anime = resolveAnime(entry)
        val airing = Instant.ofEpochSecond(entry.airingAt).atZone(ZoneId.systemDefault())
        return CalendarEpisodeUi(
            anime = anime.toHomeAnimeUi(),
            episode = entry.episode.coerceAtLeast(1),
            timeLabel = airing.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
            isNew = true,
        )
    }

    val dayModels = days.map { date ->
        CalendarDayUi(
            key = date.toString(),
            label = if (date == today) "Today" else date.dayOfWeek.name.take(3).uppercase(Locale.ENGLISH),
            dateLabel = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)),
            episodes = schedules
                .filter { Instant.ofEpochSecond(it.airingAt).atZone(ZoneId.systemDefault()).toLocalDate() == date }
                .sortedBy { it.airingAt }
                .map(::toUi),
        )
    }
    val updates = schedules
        .filter { it.airingAt <= Instant.now().epochSecond }
        .sortedByDescending { it.airingAt }
        .take(8)
        .map(::toUi)

    CalendarV1Screen(
        state = CalendarUiState(days = dayModels, selectedDayKey = today.toString(), updates = updates),
        onAnimeClick = { selected ->
            val matched = animeList.firstOrNull {
                it.animeGroupId.ifBlank { it.title } == selected.id || it.title.equals(selected.title, true)
            }
            if (matched != null) {
                onAnimeClick(matched)
            } else {
                val fallbackEntry = schedules.firstOrNull {
                    normalizeCalendarTitle(it.title) == normalizeCalendarTitle(selected.title)
                }
                if (fallbackEntry != null) onAnimeClick(resolveAnime(fallbackEntry))
            }
        },
    )
}

private fun Anime.toHomeAnimeUi() = HomeAnimeUi(
    id = animeGroupId.ifBlank { title },
    title = title,
    latestEpisode = latestEpisode,
    rating = rating,
    genre = genre,
    status = status,
)

private fun AniListScheduleEntry.toCalendarAnime(): Anime {
    val scheduleTitle = title.trim()
    return Anime(
        title = scheduleTitle,
        latestEpisode = episode.coerceAtLeast(1),
        genre = genres.joinToString(", ").ifBlank { "Unknown" },
        description = "Anime dari jadwal AniList. Detail dan episode akan dimuat dari provider KakaAnime.",
        studio = "Unknown",
        season = "Ongoing",
        year = "—",
        type = format?.replace('_', ' ')?.lowercase(Locale.ENGLISH)?.replaceFirstChar { it.uppercase() } ?: "TV",
        status = "Ongoing",
        rating = score?.let { "%.1f".format(Locale.ENGLISH, it / 10.0) } ?: "—",
        animeGroupId = scheduleTitle,
        searchAliases = listOf(scheduleTitle),
    )
}

private fun Anime.matchesCalendarEntry(entry: AniListScheduleEntry): Boolean {
    val target = normalizeCalendarTitle(entry.title)
    val own = sequenceOf(title, *searchAliases.toTypedArray()).map(::normalizeCalendarTitle).toSet()
    return target in own || normalizeCalendarTitle(stripSeasonMarker(entry.title)) == normalizeCalendarTitle(stripSeasonMarker(title))
}

private fun normalizeCalendarTitle(value: String) = value.trim().lowercase(Locale.ENGLISH).replace(Regex("\\s+"), " ")
private fun stripSeasonMarker(value: String) = value.trim().replace(Regex("(?i)\\bseason\\s+\\d+\\b"), " ").replace(Regex("(?i)\\bs\\d+\\b"), " ").replace(Regex("\\s+"), " ").trim()
