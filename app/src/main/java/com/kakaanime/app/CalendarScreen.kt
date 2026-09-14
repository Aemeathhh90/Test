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

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item { CalendarHeader(selectedDate, today) }
        item {
            LazyRow(state = dayListState, horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(days, key = { it.date.toString() }) { day -> ScheduleDayCard(day, selectedDate == day.date) { selectedDate = day.date } }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (selectedDate == today) "Today" else formatLongDate(selectedDate), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
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
                val calendarAnime = animeList
                    .filter { it.matchesCalendarEntry(entry) }
                    .sortedWith(compareBy<Anime> { seasonNumberFromTitle(entry.title) != null && it.seasonNumber != seasonNumberFromTitle(entry.title) })
                    .firstOrNull()
                    ?: entry.toAnime()
                ScheduleTimelineItem(entry, true, calendarAnime.title in favoriteTitles) { onAnimeClick(calendarAnime) }
            }
        }
    }
}

private fun Anime.matchesCalendarEntry(entry: AniListScheduleEntry): Boolean {
    val entryTitle = normalizeCalendarTitle(entry.title)
    val ownTitles = sequenceOf(title, *searchAliases.toTypedArray()).map(::normalizeCalendarTitle).filter { it.isNotBlank() }.toSet()
    if (entryTitle in ownTitles) return true
    val entryGroupTitle = normalizeCalendarTitle(stripSeasonMarker(entry.title))
    val ownGroupTitle = normalizeCalendarTitle(stripSeasonMarker(title))
    val aliasGroupTitles = searchAliases.asSequence().map { normalizeCalendarTitle(stripSeasonMarker(it)) }.toSet()
    return entryGroupTitle.isNotBlank() && (entryGroupTitle == ownGroupTitle || entryGroupTitle in aliasGroupTitles)
}

private fun AniListScheduleEntry.toAnime(): Anime {
    val scheduleTitle = title.trim()
    val seasonNumber = seasonNumberFromTitle(scheduleTitle)
    val groupTitle = stripSeasonMarker(scheduleTitle).trim().ifBlank { scheduleTitle }
    return Anime(
        title = scheduleTitle, latestEpisode = episode.coerceAtLeast(1), genre = genres.joinToString(", ").ifBlank { "Unknown" },
        description = "Anime dari jadwal AniList. Detail dan daftar episode akan dimuat dari provider KakaAnime.", studio = "Unknown",
        season = if (seasonNumber != null) "Season $seasonNumber" else "Ongoing", year = "—",
        type = format?.replace('_', ' ')?.lowercase(Locale.ENGLISH)?.replaceFirstChar { it.uppercase() } ?: "TV",
        status = "Ongoing", rating = score?.let { "%.1f".format(Locale.ENGLISH, it / 10.0) } ?: "—",
        animeGroupId = groupTitle, seasonNumber = seasonNumber, seasonTitle = seasonNumber?.let { "Season $it" }, searchAliases = listOf(scheduleTitle, groupTitle),
    )
}

private fun normalizeCalendarTitle(value: String): String = value.trim().lowercase(Locale.ENGLISH).replace(Regex("\\s+"), " ")
private fun stripSeasonMarker(value: String): String = value.trim().replace(Regex("(?i)\\bseason\\s+\\d+\\b"), " ").replace(Regex("(?i)\\bs\\d+\\b"), " ").replace(Regex("\\s+"), " ").trim()
private fun seasonNumberFromTitle(value: String): Int? = Regex("(?i)\\b(?:season\\s*|s)\\s*(\\d+)\\b").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()

@Composable private fun CalendarHeader(selectedDate: LocalDate, today: LocalDate) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(42.dp), RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Calendar", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (selectedDate == today) "Anime yang tayang hari ini" else formatLongDate(selectedDate), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selectedDate != today) TextButton(onClick = { }) { }
    }
}

@Composable private fun ScheduleDayCard(day: ScheduleDay, selected: Boolean, onClick: () -> Unit) {
    val dayName = day.date.dayOfWeek.name.take(3).uppercase(Locale.ENGLISH)
    val monthName = day.date.month.name.take(3).replaceFirstChar { it.uppercase() }
    Surface(
        Modifier.width(64.dp).height(76.dp).clickable { onClick() }, RoundedCornerShape(17.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(Modifier.fillMaxSize().padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(dayName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(day.date.dayOfMonth.toString(), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            Text(if (day.isToday) "TODAY" else monthName, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun ScheduleTimelineItem(entry: AniListScheduleEntry, clickable: Boolean, isFavorite: Boolean, onClick: () -> Unit) {
    val now = Instant.now(); val airing = Instant.ofEpochSecond(entry.airingAt)
    val time = airing.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")); val isAired = airing <= now
    Row(Modifier.fillMaxWidth().clickable(enabled = clickable, onClick = onClick), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(43.dp).padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (isAired) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f) else MaterialTheme.colorScheme.primary))
        }
        Box(Modifier.width(11.dp).height(112.dp)) {
            Box(Modifier.align(Alignment.TopCenter).padding(top = 24.dp).width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .14f)))
        }
        Surface(Modifier.weight(1f).heightIn(min = 104.dp), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .34f), tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                if (entry.imageUrl != null) AsyncImage(entry.imageUrl, entry.title, Modifier.size(66.dp, 88.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop) else PosterPlaceholder(entry.title)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(entry.title, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (isFavorite) Icon(Icons.Outlined.Favorite, "Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 5.dp).size(15.dp))
                        Icon(Icons.Outlined.MoreVert, "Episode options", modifier = Modifier.padding(start = 2.dp).size(18.dp))
                    }
                    Text("Episode ${entry.episode}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(metadataText(entry), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!isAired) CountdownBadge(countdownText(now, airing)) else StatusBadge(true)
                }
            }
        }
    }
}

@Composable private fun StatusBadge(isAired: Boolean) { Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)) { Text(if (isAired) "Aired" else "Airing Soon", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun CountdownBadge(text: String) { Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) { Text("$text left", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } }
private fun metadataText(entry: AniListScheduleEntry): String { val format = entry.format?.replace('_', ' ')?.lowercase(Locale.ENGLISH)?.replaceFirstChar { it.uppercase() } ?: "TV"; val score = entry.score?.let { "★ ${"%.1f".format(Locale.ENGLISH, it / 10.0)}" }; val duration = entry.durationMinutes?.let { "${it}m" }; return listOf(format, score, duration).filterNotNull().joinToString(" · ") }
private fun countdownText(now: Instant, airing: Instant): String { val seconds = Duration.between(now, airing).seconds; if (seconds <= 0L) return "Now"; val days = seconds / 86400L; val hours = (seconds % 86400L) / 3600L; val minutes = (seconds % 3600L) / 60L; return when { days > 0 -> "${days}d ${hours}h"; hours > 0 -> "${hours}h ${minutes}m"; minutes > 0 -> "${minutes}m"; else -> "<1m" } }
@Composable private fun PosterPlaceholder(title: String) { Box(Modifier.size(66.dp, 88.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .16f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) { Text(title.take(2).uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } }
@Composable private fun EmptyScheduleState() { Column(Modifier.fillMaxWidth().padding(vertical = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp)); Spacer(Modifier.height(8.dp)); Text("No anime scheduled", fontWeight = FontWeight.SemiBold); Text("AniList did not return any episodes for this day.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
private fun formatLongDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
