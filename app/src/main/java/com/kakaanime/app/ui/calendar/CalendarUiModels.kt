package com.kakaanime.app.ui.calendar

import com.kakaanime.app.ui.home.HomeAnimeUi

data class CalendarEpisodeUi(
    val anime: HomeAnimeUi,
    val episode: Int,
    val timeLabel: String = "",
    val isNew: Boolean = true,
)

data class CalendarDayUi(
    val key: String,
    val label: String,
    val dateLabel: String,
    val episodes: List<CalendarEpisodeUi> = emptyList(),
)

data class CalendarUiState(
    val days: List<CalendarDayUi> = emptyList(),
    val selectedDayKey: String = "",
    val updates: List<CalendarEpisodeUi> = emptyList(),
)
