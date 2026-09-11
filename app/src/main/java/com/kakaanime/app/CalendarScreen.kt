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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private data class ScheduleDay(val label: String, val day: String, val month: String, val isToday: Boolean = false)
private data class ScheduleEntry(val anime: Anime, val time: String, val period: String, val episode: Int, val status: String, val countdown: String? = null)

private val scheduleDays = listOf(
    ScheduleDay("MON", "23", "Jun"), ScheduleDay("TUE", "24", "Jun"), ScheduleDay("WED", "25", "Jun"),
    ScheduleDay("THU", "26", "Jun"), ScheduleDay("FRI", "27", "Jun", true), ScheduleDay("SAT", "28", "Jun"),
    ScheduleDay("SUN", "29", "Jun")
)

@Composable
fun CalendarScreen(animeList: List<Anime>, onAnimeClick: (Anime) -> Unit, favoriteTitles: Set<String> = setOf("One Piece")) {
    var selectedDayIndex by remember { mutableStateOf(4) }
    val selectedDay = scheduleDays[selectedDayIndex]
    val entries = remember(animeList, selectedDayIndex) { buildScheduleEntries(animeList, selectedDayIndex) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { CalendarHeader() }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(scheduleDays.size) { index -> ScheduleDayCard(scheduleDays[index], selectedDayIndex == index) { selectedDayIndex = index } }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (selectedDay.isToday) "Today · ${selectedDay.month} ${selectedDay.day}" else "${selectedDay.label} · ${selectedDay.month} ${selectedDay.day}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Jadwal episode anime", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${entries.size} episode${if (entries.size == 1) "" else "s"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (entries.isEmpty()) item { EmptyScheduleState() }
        else items(entries) { entry -> ScheduleTimelineItem(entry, entry.anime.title in favoriteTitles) { onAnimeClick(entry.anime) } }
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
    Surface(
        modifier = Modifier.width(72.dp).height(88.dp).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(Modifier.fillMaxSize().padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(day.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(day.day, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(day.month, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (selected) Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun ScheduleTimelineItem(entry: ScheduleEntry, isFavorite: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }, verticalAlignment = Alignment.Top) {
        Column(Modifier.width(64.dp).padding(top = 14.dp), horizontalAlignment = Alignment.End) {
            Text(entry.time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(entry.period, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.width(24.dp).height(132.dp)) {
            Box(Modifier.align(Alignment.TopCenter).padding(top = 20.dp).size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.align(Alignment.TopCenter).padding(top = 28.dp).width(1.dp).height(104.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .22f)))
        }
        Surface(Modifier.weight(1f), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f), tonalElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                PosterPlaceholder(entry.anime)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.anime.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.weight(1f))
                        if (isFavorite) Icon(Icons.Outlined.Favorite, "Favorite", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                    }
                    Text("EP ${entry.episode}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("${entry.anime.type}  •  ★ ${entry.anime.rating}  •  ${entry.anime.year}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Text(entry.anime.genre, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(Modifier.width(6.dp))
                StatusBadge(entry.status, entry.countdown)
            }
        }
    }
}

@Composable
private fun PosterPlaceholder(anime: Anime) {
    Box(Modifier.size(width = 58.dp, height = 78.dp).clip(RoundedCornerShape(11.dp)).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = .16f), MaterialTheme.colorScheme.surface))), contentAlignment = Alignment.Center) {
        Text(anime.title.take(2).uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun StatusBadge(status: String, countdown: String?) {
    Column(horizontalAlignment = Alignment.End) {
        Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
            Text(status, Modifier.padding(horizontal = 8.dp, vertical = 6.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        if (countdown != null) { Spacer(Modifier.height(4.dp)); Text(countdown, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun EmptyScheduleState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(8.dp))
        Text("Belum ada jadwal anime", fontWeight = FontWeight.SemiBold)
        Text("Coba pilih hari lain.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun buildScheduleEntries(animeList: List<Anime>, selectedDayIndex: Int): List<ScheduleEntry> {
    if (animeList.isEmpty() || selectedDayIndex != 4) return emptyList()
    return animeList.mapIndexed { index, anime ->
        if (index == 0) ScheduleEntry(anime, "08:00", "AM", anime.latestEpisode, "AVAILABLE")
        else ScheduleEntry(anime, "17:30", "PM", anime.latestEpisode + 1, "AIRING SOON", "4j 44m lagi")
    }
}
