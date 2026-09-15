package com.kakaanime.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.kakaanime.app.ui.home.HomeAnimeUi

@Composable
fun CalendarV1Screen(
    state: CalendarUiState,
    onAnimeClick: (HomeAnimeUi) -> Unit = {},
) {
    var selectedKey by remember(state.selectedDayKey, state.days) {
        mutableStateOf(state.selectedDayKey.ifBlank { state.days.firstOrNull()?.key.orEmpty() })
    }
    val selectedDay = state.days.firstOrNull { it.key == selectedKey }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Calendar", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Jadwal episode anime terbaru.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.days) { _, day ->
                    FilterChip(
                        selected = selectedKey == day.key,
                        onClick = { selectedKey = day.key },
                        label = { Column { Text(day.label, fontSize = 11.sp); Text(day.dateLabel, fontSize = 9.sp) } },
                    )
                }
            }
        }
        item { Text(selectedDay?.label ?: "Jadwal", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
        if (selectedDay == null || selectedDay.episodes.isEmpty()) {
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada episode di hari ini.", fontWeight = FontWeight.SemiBold)
                        Text("Cek hari lain untuk jadwal berikutnya.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(selectedDay.episodes, key = { "${selectedDay.key}-${it.anime.id}-${it.episode}" }) { item ->
                CalendarEpisodeRow(item) { onAnimeClick(item.anime) }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.NewReleases, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("New Updates", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        if (state.updates.isEmpty()) {
            item { Text("Belum ada update baru.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.updates.take(8), key = { "update-${it.anime.id}-${it.episode}" }) { item ->
                CalendarEpisodeRow(item) { onAnimeClick(item.anime) }
            }
        }
    }
}

@Composable
private fun CalendarEpisodeRow(item: CalendarEpisodeUi, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .36f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .13f)) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EP", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(item.episode.toString(), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.anime.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(item.timeLabel.ifBlank { "Episode ${item.episode}" }, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.isNew) {
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .14f)) {
                    Text("NEW", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
