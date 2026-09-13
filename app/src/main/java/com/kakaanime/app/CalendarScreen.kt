package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.data.AniListCalendarService
import com.kakaanime.app.data.AniListScheduleEntry
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ScheduleDay(val date: LocalDate, val isToday: Boolean)

@Composable
fun CalendarScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    favoriteTitles: Set<String> = emptySet()
) {
    val service = remember { AniListCalendarService() }
    val today = remember { LocalDate.now() }
    val days = remember(today) {
        (0L..6L).map { date ->
            val value = today.plusDays(date)
            ScheduleDay(value, value == today)
        }
    }
    var selectedDate by remember(today) { mutableStateOf(today) }
    var schedules by remember { mutableStateOf<List<AniListScheduleEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        schedules = service.getSchedule()
        loading = false
    }

    val entries = remember(schedules, selectedDate) {
        schedules.filter {
            Instant.ofEpochSecond(it.airingAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == selectedDate
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CalendarHeader() }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(days) { day ->
                    ScheduleDayCard(day, selectedDate == day.date) { selectedDate = day.date }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (selectedDate == today) "Today" else formatDate(selectedDate),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (selectedDate == today) formatDate(selectedDate) else "Jadwal episode yang tayang",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!loading) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
                    ) {
                        Text(
                            "${entries.size} Ep",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (loading) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        } else if (entries.isEmpty()) {
            item { EmptyScheduleState() }
        } else {
            items(entries, key = { "${it.id}-${it.episode}-${it.airingAt}" }) { entry ->
                val matchedAnime = animeList.firstOrNull { anime ->
                    anime.title.equals(entry.title, ignoreCase = true)
                }
                ScheduleTimelineItem(
                    entry = entry,
                    isFavorite = matchedAnime?.title in favoriteTitles,
                    onClick = { matchedAnime?.let(onAnimeClick) }
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Calendar", fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Text(
                "Anime yang akan tayang",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f)
        ) {
            Icon(Icons.Outlined.Search, "Cari jadwal", Modifier.padding(11.dp))
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f)
        ) {
            Icon(Icons.Outlined.MoreVert, "Opsi jadwal", Modifier.padding(11.dp))
        }
    }
}

@Composable
private fun ScheduleDayCard(day: ScheduleDay, selected: Boolean, onClick: () -> Unit) {
    val dayName = day.date.dayOfWeek.name.take(3).lowercase(Locale.ENGLISH).replaceFirstChar { it.uppercase() }
    Surface(
        modifier = Modifier.width(66.dp).height(82.dp).clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = .16f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f)
        },
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                day.date.dayOfMonth.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(day.date.month.name.take(3), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (day.isToday) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            } else if (selected) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .55f)))
            } else {
                Spacer(Modifier.size(5.dp))
            }
        }
    }
}

@Composable
private fun ScheduleTimelineItem(entry: AniListScheduleEntry, isFavorite: Boolean, onClick: () -> Unit) {
    val now = Instant.now()
    val airing = Instant.ofEpochSecond(entry.airingAt)
    val isAiringToday = airing.atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
    val time = airing.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    val countdown = countdownText(now, airing)

    Row(
        Modifier.fillMaxWidth().clickable(enabled = onClick != {}) { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.width(54.dp).padding(top = 12.dp), horizontalAlignment = Alignment.End) {
            Text(time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("LOCAL", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(Modifier.width(20.dp).height(126.dp)) {
            Box(
                Modifier.align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isAiringToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f))
            )
            Box(
                Modifier.align(Alignment.TopCenter)
                    .padding(top = 26.dp)
                    .width(1.dp)
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .16f))
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .38f),
            tonalElevation = 1.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.imageUrl != null) {
                    AsyncImage(
                        model = entry.imageUrl,
                        contentDescription = entry.title,
                        modifier = Modifier.size(width = 58.dp, height = 78.dp).clip(RoundedCornerShape(13.dp))
                    )
                } else {
                    PosterPlaceholder(entry.title)
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        if (isFavorite) {
                            Icon(
                                Icons.Outlined.Favorite,
                                "Favorite",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Ep ${entry.episode}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(7.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .11f)
                        ) {
                            Text(
                                countdown,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        if (isAiringToday) "Airing today · $time" else formatDateTime(entry.airingAt),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun countdownText(now: Instant, airing: Instant): String {
    val seconds = Duration.between(now, airing).seconds
    if (seconds <= 0L) return "NOW"
    val days = seconds / 86_400L
    val hours = (seconds % 86_400L) / 3_600L
    val minutes = (seconds % 3_600L) / 60L
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

@Composable
private fun PosterPlaceholder(title: String) {
    Box(
        Modifier.size(width = 58.dp, height = 78.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .16f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title.take(2).uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyScheduleState() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.CalendarMonth,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("Belum ada jadwal anime", fontWeight = FontWeight.SemiBold)
        Text(
            "AniList tidak mengembalikan episode untuk hari ini.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))

private fun formatDateTime(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale.ENGLISH))
