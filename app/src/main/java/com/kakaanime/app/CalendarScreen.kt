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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ScheduleDay(val date: java.time.LocalDate, val isToday: Boolean)

@Composable
fun CalendarScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    favoriteTitles: Set<String> = setOf("One Piece")
) {
    val service = remember { AniListCalendarService() }
    val today = remember { java.time.LocalDate.now() }
    val days = remember(today) { (0L..6L).map { today.plusDays(it) }.map { ScheduleDay(it, it == today) } }
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
            Instant.ofEpochSecond(it.airingAt).atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { CalendarHeader() }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(days) { day ->
                    ScheduleDayCard(day, selectedDate == day.date) { selectedDate = day.date }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (selectedDate == today) "Today · ${formatDate(selectedDate)}" else formatDate(selectedDate),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Jadwal episode anime dari AniList", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!loading) Text("${entries.size} episode${if (entries.size == 1) "" else "s"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        } else if (entries.isEmpty()) {
            item { EmptyScheduleState() }
        } else {
            items(entries, key = { "${it.id}-${it.episode}-${it.airingAt}" }) { entry ->
                val matchedAnime = animeList.firstOrNull { it.title.equals(entry.title, ignoreCase = true) }
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
            Text("Schedule", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Pantau episode anime yang akan tayang", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f)) { Icon(Icons.Outlined.Search, "Cari jadwal", Modifier.padding(11.dp)) }
        Spacer(Modifier.width(8.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f)) { Icon(Icons.Outlined.MoreVert, "Opsi jadwal", Modifier.padding(11.dp)) }
    }
}

@Composable
private fun ScheduleDayCard(day: ScheduleDay, selected: Boolean, onClick: () -> Unit) {
    val dayName = day.date.dayOfWeek.name.take(3)
    Surface(
        modifier = Modifier.width(72.dp).height(88.dp).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(Modifier.fillMaxSize().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(dayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(day.date.dayOfMonth.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(day.date.month.name.take(3), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (selected) Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun ScheduleTimelineItem(entry: AniListScheduleEntry, isFavorite: Boolean, onClick: () -> Unit) {
    val time = Instant.ofEpochSecond(entry.airingAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    Row(Modifier.fillMaxWidth().clickable(enabled = onClick != {}) { onClick() }, verticalAlignment = Alignment.Top) {
        Column(Modifier.width(64.dp).padding(top = 14.dp), horizontalAlignment = Alignment.End) {
            Text(time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("LOCAL", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.width(24.dp).height(132.dp)) {
            Box(Modifier.align(Alignment.TopCenter).padding(top = 20.dp).size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.align(Alignment.TopCenter).padding(top = 28.dp).width(1.dp).height(104.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .22f)))
        }
        Surface(Modifier.weight(1f), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f), tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                if (entry.imageUrl != null) {
                    AsyncImage(model = entry.imageUrl, contentDescription = entry.title, modifier = Modifier.size(width = 58.dp, height = 78.dp).clip(RoundedCornerShape(11.dp)))
                } else {
                    PosterPlaceholder(entry.title)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.weight(1f))
                        if (isFavorite) Icon(Icons.Outlined.Favorite, "Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                    }
                    Text("EP ${entry.episode}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("AniList · ${formatDateTime(entry.airingAt)}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PosterPlaceholder(title: String) {
    Box(Modifier.size(width = 58.dp, height = 78.dp).clip(RoundedCornerShape(11.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .16f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) {
        Text(title.take(2).uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyScheduleState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(8.dp))
        Text("Belum ada jadwal anime", fontWeight = FontWeight.SemiBold)
        Text("AniList tidak mengembalikan episode untuk hari ini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDate(date: java.time.LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))

private fun formatDateTime(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale.ENGLISH))
