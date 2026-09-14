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

private data class WatchRoomUi(
    val id: String,
    val title: String,
    val animeTitle: String,
    val episode: Int,
    val host: String,
    val members: Int,
    val capacity: Int,
    val genres: List<String>,
)

/**
 * Watch Together room lobby UI foundation.
 *
 * Room creation/join state is still local-only presentation behavior.
 * The room screen is now reachable from the lobby, but real membership,
 * invites, playback sync and server-side room state require the future Social backend.
 */
@Composable
fun WatchTogetherScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var roomTitle by remember { mutableStateOf("") }
    var createdRooms by remember { mutableStateOf<List<WatchRoomUi>>(emptyList()) }
    var selectedRoom by remember { mutableStateOf<WatchRoomUi?>(null) }

    val activeRooms = remember {
        listOf(
            WatchRoomUi("KA-1150", "One Piece E1150", "One Piece", 1150, "Shin", 5, 10, listOf("Action", "Adventure")),
            WatchRoomUi("KA-012", "Solo Leveling E12", "Solo Leveling", 12, "Kael", 3, 8, listOf("Action", "Fantasy")),
            WatchRoomUi("KA-020", "Frieren E20", "Frieren", 20, "Mizu", 4, 6, listOf("Fantasy", "Drama")),
        )

    selectedRoom?.let { room ->
        WatchRoomScreen(
            roomTitle = room.title,
            animeTitle = room.animeTitle,
            episodeNumber = room.episode,
            roomCode = room.id,
            onBack = { selectedRoom = null },
            onLeaveRoom = { selectedRoom = null },
        )
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("‹  Kembali") }
                    Spacer(Modifier.weight(1f))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("?", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Column {
                    Text("Watch Together", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Buat room, ajak teman, lalu nonton anime bersama.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("▶", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text("Nonton bareng, satu room", fontWeight = FontWeight.Bold)
                                Text(
                                    "Temukan room atau mulai sesi baru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { showCreateDialog = true }, modifier = Modifier.weight(1f)) { Text("Create Room") }
                            OutlinedButton(onClick = { showJoinDialog = true }, modifier = Modifier.weight(1f)) { Text("Join Room") }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Active Rooms") })
                    FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("My Rooms") })
                }
            }

            if (selectedTab == 0) {
                item { RoomSectionHeader("Active Rooms", "${activeRooms.size} room tersedia") }
                items(activeRooms, key = { it.id }) { room ->
                    WatchRoomCard(room, onJoin = { selectedRoom = room })
                }
            } else if (createdRooms.isEmpty()) {
                item { WatchTogetherEmptyState("Belum ada room buatanmu.", "Buat room untuk mulai menonton bersama.", onCreate = { showCreateDialog = true }) }
            } else {
                item { RoomSectionHeader("My Rooms", "Room yang kamu buat di sesi ini") }
                items(createdRooms, key = { it.id }) { room -> WatchRoomCard(room, onJoin = { selectedRoom = room }) }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("i", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Room online dan sinkronisasi playback akan aktif setelah Social backend terhubung.",
                            modifier = Modifier.padding(start = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Room") },
            text = {
                Column {
                    Text("UI foundation • sesi lokal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        singleLine = true,
                        label = { Text("Nama room") },
                        placeholder = { Text("Contoh: One Piece malam ini") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = roomTitle.trim().length >= 3,
                    onClick = {
                        val normalized = roomTitle.trim()
                        val newRoom = WatchRoomUi(
                            id = "MY-${System.currentTimeMillis().toString().takeLast(4)}",
                            title = normalized,
                            animeTitle = "One Piece",
                            episode = 1,
                            host = "Shin",
                            members = 1,
                            capacity = 10,
                            genres = emptyList(),
                        )
                        createdRooms = listOf(newRoom) + createdRooms
                        roomTitle = ""
                        showCreateDialog = false
                        selectedTab = 1
                        selectedRoom = newRoom
                    },
                ) { Text("Buat Room") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Batal") } },
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join Room") },
            text = {
                Column {
                    Text("Masukkan kode room dari teman.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it.uppercase(Locale.ROOT).take(12) },
                        singleLine = true,
                        label = { Text("Kode room") },
                        placeholder = { Text("Contoh: KA-1150") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = joinCode.trim().length >= 4,
                    onClick = {
                        val normalized = joinCode.trim()
                        val room = activeRooms.firstOrNull { it.id.equals(normalized, ignoreCase = true) }
                        showJoinDialog = false
                        joinCode = ""
                        if (room != null) selectedRoom = room
                    },
                ) { Text("Gabung") }
            },
            dismissButton = { TextButton(onClick = { showJoinDialog = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun RoomSectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WatchRoomCard(room: WatchRoomUi, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("▶", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(room.title, fontWeight = FontWeight.Bold)
                    Text("Episode ${room.episode} • Host ${room.host}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text("LIVE", modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥 ${room.members}/${room.capacity}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                if (room.genres.isNotEmpty()) Text(room.genres.take(2).joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(10.dp))
                Button(onClick = onJoin) { Text("Join") }
            }
        }
    }
}

@Composable
private fun WatchTogetherEmptyState(title: String, subtitle: String, onCreate: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text("▶", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onCreate) { Text("Create Room") }
        }
    }
}
