package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
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
    val days = remember(today) { (-7L..30L).map { offset -> val date = today.plusDays(offset); ScheduleDay(date, date == today) } }
    var selectedDate by remember(today) { mutableStateOf(today) }
    var schedules by remember { mutableStateOf<List<AniListScheduleEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val dayListState = rememberLazyListState(initialFirstVisibleItemIndex = 7)

    LaunchedEffect(Unit) {
        loading = true
        schedules = service.getSchedule(from = today.minusDays(7).atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), days = 37)
        loading = false
    }
    LaunchedEffect(selectedDate) {
        val index = days.indexOfFirst { it.date == selectedDate }
        if (index >= 0) dayListState.animateScrollToItem((index - 2).coerceAtLeast(0))
    }
    val entries = remember(schedules, selectedDate) { schedules.filter { Instant.ofEpochSecond(it.airingAt).atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate } }

    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 112.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CalendarHeader() }
        item {
            LazyRow(state = dayListState, horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(days, key = { it.date.toString() }) { day -> ScheduleDayCard(day, selectedDate == day.date) { selectedDate = day.date } }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (selectedDate == today) "Today · ${formatLongDate(selectedDate)}" else formatLongDate(selectedDate), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${entries.size} episodes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (selectedDate != today) TextButton(onClick = { selectedDate = today }) { Text("Today") }
            }
        }
        if (loading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) } }
        } else if (entries.isEmpty()) {
            item { EmptyScheduleState() }
        } else {
            items(entries, key = { "${it.id}-${it.episode}-${it.airingAt}" }) { entry ->
                val matchedAnime = animeList.firstOrNull { it.title.equals(entry.title, ignoreCase = true) }
                ScheduleTimelineItem(entry, matchedAnime != null, matchedAnime?.title in favoriteTitles) { matchedAnime?.let(onAnimeClick) }
            }
        }
    }
}

@Composable private fun CalendarHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text("Schedule", fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Track upcoming anime episodes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        IconButton(onClick = {}) { Icon(Icons.Outlined.Search, "Search schedule") }
        IconButton(onClick = {}) { Icon(Icons.Outlined.MoreVert, "Schedule options") }
    }
}

@Composable private fun ScheduleDayCard(day: ScheduleDay, selected: Boolean, onClick: () -> Unit) {
    val dayName = day.date.dayOfWeek.name.take(3).uppercase(Locale.ENGLISH)
    val monthName = day.date.month.name.take(3).replaceFirstChar { it.uppercase() }
    Surface(Modifier.width(68.dp).height(78.dp).clickable { onClick() }, RoundedCornerShape(18.dp), color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f), tonalElevation = if (selected) 2.dp else 0.dp) {
        Column(Modifier.fillMaxSize().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(dayName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(day.date.dayOfMonth.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(if (day.isToday) "Today" else monthName, fontSize = 9.sp, fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal, color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun ScheduleTimelineItem(entry: AniListScheduleEntry, clickable: Boolean, isFavorite: Boolean, onClick: () -> Unit) {
    val now = Instant.now(); val airing = Instant.ofEpochSecond(entry.airingAt)
    val time = airing.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")); val isAired = airing <= now
    Row(Modifier.fillMaxWidth().clickable(enabled = clickable, onClick = onClick), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(48.dp).padding(top = 11.dp), horizontalAlignment = Alignment.End) { Text(time, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        TimelineRail(isAired)
        Surface(Modifier.weight(1f).heightIn(min = 108.dp), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .36f), tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                if (entry.imageUrl != null) AsyncImage(entry.imageUrl, entry.title, Modifier.size(64.dp, 86.dp).clip(RoundedCornerShape(12.dp))) else PosterPlaceholder(entry.title)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(entry.title, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (isFavorite) Icon(Icons.Outlined.Favorite, "Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 5.dp).size(15.dp))
                        Icon(Icons.Outlined.MoreVert, "Episode options", modifier = Modifier.padding(start = 2.dp).size(18.dp))
                    }
                    Text("Episode ${entry.episode}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(metadataText(entry), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (entry.genres.isNotEmpty()) Text(entry.genres.take(3).joinToString(" · "), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { StatusBadge(isAired); if (!isAired) CountdownBadge(countdownText(now, airing)) }
                }
            }
        }
    }
}

@Composable private fun TimelineRail(isAired: Boolean) { Box(Modifier.width(20.dp).height(108.dp)) { Box(Modifier.align(Alignment.TopCenter).padding(top = 20.dp).width(1.dp).height(88.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .18f))); Box(Modifier.align(Alignment.TopCenter).padding(top = 16.dp).size(9.dp).clip(CircleShape).background(if (isAired) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .48f) else MaterialTheme.colorScheme.primary)) } }
@Composable private fun StatusBadge(isAired: Boolean) { Surface(RoundedCornerShape(50), color = if (isAired) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f) else MaterialTheme.colorScheme.primary.copy(alpha = .13f)) { Text(if (isAired) "Aired" else "Airing Soon", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isAired) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary) } }
@Composable private fun CountdownBadge(text: String) { Surface(RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)) { Text("$text left", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } }
private fun metadataText(entry: AniListScheduleEntry): String { val format = entry.format?.replace('_', ' ')?.lowercase(Locale.ENGLISH)?.replaceFirstChar { it.uppercase() } ?: "TV"; val score = entry.score?.let { "★ ${"%.1f".format(Locale.ENGLISH, it / 10.0)}" }; val duration = entry.durationMinutes?.let { "${it}m" }; return listOf(format, score, duration).filterNotNull().joinToString(" · ") }
private fun countdownText(now: Instant, airing: Instant): String { val seconds = Duration.between(now, airing).seconds; if (seconds <= 0L) return "Now"; val days = seconds / 86400L; val hours = (seconds % 86400L) / 3600L; val minutes = (seconds % 3600L) / 60L; return when { days > 0 -> "${days}d ${hours}h"; hours > 0 -> "${hours}h ${minutes}m"; minutes > 0 -> "${minutes}m"; else -> "<1m" } }
@Composable private fun PosterPlaceholder(title: String) { Box(Modifier.size(64.dp, 86.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .16f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) { Text(title.take(2).uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } }
@Composable private fun EmptyScheduleState() { Column(Modifier.fillMaxWidth().padding(vertical = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp)); Spacer(Modifier.height(8.dp)); Text("No anime scheduled", fontWeight = FontWeight.SemiBold); Text("AniList did not return any episodes for this day.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
private fun formatLongDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
