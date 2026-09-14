package com.kakaanime.app

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

private data class WatchRoomUi(val id: String, val title: String, val animeTitle: String, val episode: Int, val host: String, val members: Int, val capacity: Int, val genres: List<String>)

@Composable
fun WatchTogetherScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var roomTitle by remember { mutableStateOf("") }
    var createdRooms by remember { mutableStateOf<List<WatchRoomUi>>(emptyList()) }
    var selectedRoom by remember { mutableStateOf<WatchRoomUi?>(null) }
    val activeRooms = remember { listOf(
        WatchRoomUi("KA-1150", "One Piece E1150", "One Piece", 1150, "Shin", 5, 10, listOf("Action", "Adventure")),
        WatchRoomUi("KA-012", "Solo Leveling E12", "Solo Leveling", 12, "Kael", 3, 8, listOf("Action", "Fantasy")),
        WatchRoomUi("KA-020", "Frieren E20", "Frieren", 20, "Mizu", 4, 6, listOf("Fantasy", "Drama")),
    )

    if (selectedRoom != null) {
        val room = selectedRoom!!
        WatchRoomScreen(room.title, room.animeTitle, room.episode, room.id, { selectedRoom = null }, { selectedRoom = null })
    } else {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("‹  Kembali") }; Spacer(Modifier.weight(1f)); Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) { Text("?", Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontWeight = FontWeight.Bold) } } }
                item { Column { Text("Watch Together", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Buat room, ajak teman, lalu nonton anime bersama.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                item { Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) { Text("Nonton bareng, satu room", fontWeight = FontWeight.Bold); Text("Temukan room atau mulai sesi baru.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer); Spacer(Modifier.height(14.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { Button({ showCreateDialog = true }, Modifier.weight(1f)) { Text("Create Room") }; OutlinedButton({ showJoinDialog = true }, Modifier.weight(1f)) { Text("Join Room") } } } } }
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selectedTab == 0, { selectedTab = 0 }, label = { Text("Active Rooms") }); FilterChip(selectedTab == 1, { selectedTab = 1 }, label = { Text("My Rooms") }) } }
                if (selectedTab == 0) { item { Text("Active Rooms · ${activeRooms.size} room tersedia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }; items(activeRooms, key = { it.id }) { room -> WatchRoomCard(room) { selectedRoom = room } } }
                else if (createdRooms.isEmpty()) item { EmptyRoomState { showCreateDialog = true } }
                else { item { Text("My Rooms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }; items(createdRooms, key = { it.id }) { room -> WatchRoomCard(room) { selectedRoom = room } } }
                item { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) { Text("Room online dan sinkronisasi playback akan aktif setelah Social backend terhubung.", Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
        if (showCreateDialog) AlertDialog(onDismissRequest = { showCreateDialog = false }, title = { Text("Create Room") }, text = { OutlinedTextField(roomTitle, { roomTitle = it }, singleLine = true, label = { Text("Nama room") }, modifier = Modifier.fillMaxWidth()) }, confirmButton = { TextButton(enabled = roomTitle.trim().length >= 3, onClick = { val room = WatchRoomUi("MY-${System.currentTimeMillis().toString().takeLast(4)}", roomTitle.trim(), "One Piece", 1, "Shin", 1, 10, emptyList()); createdRooms = listOf(room) + createdRooms; roomTitle = ""; showCreateDialog = false; selectedTab = 1; selectedRoom = room }) { Text("Buat Room") } }, dismissButton = { TextButton({ showCreateDialog = false }) { Text("Batal") } })
        if (showJoinDialog) AlertDialog(onDismissRequest = { showJoinDialog = false }, title = { Text("Join Room") }, text = { OutlinedTextField(joinCode, { joinCode = it.uppercase(Locale.ROOT).take(12) }, singleLine = true, label = { Text("Kode room") }, modifier = Modifier.fillMaxWidth()) }, confirmButton = { TextButton(enabled = joinCode.trim().length >= 4, onClick = { val room = activeRooms.firstOrNull { it.id.equals(joinCode.trim(), true) }; showJoinDialog = false; joinCode = ""; if (room != null) selectedRoom = room }) { Text("Gabung") } }, dismissButton = { TextButton({ showJoinDialog = false }) { Text("Batal") } })
    }
}

@Composable private fun WatchRoomCard(room: WatchRoomUi, onJoin: () -> Unit) { Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) { Column(Modifier.padding(16.dp)) { Text(room.title, fontWeight = FontWeight.Bold); Text("Episode ${room.episode} • Host ${room.host}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(10.dp)); HorizontalDivider(); Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("${room.members}/${room.capacity} peserta", style = MaterialTheme.typography.labelMedium); Spacer(Modifier.weight(1f)); Button(onClick = onJoin) { Text("Join") } } } } }
@Composable private fun EmptyRoomState(onCreate: () -> Unit) { Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Belum ada room buatanmu.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Buat room untuk mulai menonton bersama.", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); OutlinedButton(onCreate) { Text("Create Room") } } } }