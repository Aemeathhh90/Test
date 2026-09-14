package com.kakaanime.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
    val episode: Int,
    val host: String,
    val members: Int,
    val capacity: Int,
    val genres: List<String>,
)

@Composable
fun WatchTogetherScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var roomTitle by remember { mutableStateOf("") }
    var createdRooms by remember { mutableStateOf<List<WatchRoomUi>>(emptyList()) }

    val activeRooms = remember {
        listOf(
            WatchRoomUi("KA-1150", "One Piece E1150", 1150, "Shin", 5, 10, listOf("Action", "Adventure")),
            WatchRoomUi("KA-012", "Solo Leveling E12", 12, "Kael", 3, 8, listOf("Action", "Fantasy")),
            WatchRoomUi("KA-020", "Frieren E20", 20, "Mizu", 4, 6, listOf("Fantasy", "Drama")),
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onBack) { Text("‹  Kembali") }
                    Spacer(Modifier.weight(1f))
                }
            }
            item {
                Column {
                    Text("Watch Together", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Nonton anime bareng teman dalam satu room.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(6.dp))
                            Text("Create Room", fontWeight = FontWeight.Bold)
                            Text("Buat room nonton baru", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = { showCreateDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Buat") }
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("⌘", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(6.dp))
                            Text("Join Room", fontWeight = FontWeight.Bold)
                            Text("Masuk dengan kode room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = { showJoinDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Gabung") }
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
                items(activeRooms, key = { it.id }) { room -> WatchRoomCard(room, onJoin = { showJoinDialog = true }) }
            } else if (createdRooms.isEmpty()) {
                item { WatchTogetherEmptyState("Belum ada room buatanmu.", "Buat room untuk mulai nonton bareng.") }
            } else {
                items(createdRooms, key = { it.id }) { room -> WatchRoomCard(room, onJoin = { }) }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Room") },
            text = {
                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { roomTitle = it },
                    singleLine = true,
                    label = { Text("Nama room") },
                    placeholder = { Text("Contoh: One Piece malam ini") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = roomTitle.trim().length >= 3,
                    onClick = {
                        val normalized = roomTitle.trim()
                        createdRooms = listOf(
                            WatchRoomUi(
                                id = "MY-${System.currentTimeMillis().toString().takeLast(4)}",
                                title = normalized,
                                episode = 1,
                                host = "Shin",
                                members = 1,
                                capacity = 10,
                                genres = emptyList(),
                            ),
                        ) + createdRooms
                        roomTitle = ""
                        showCreateDialog = false
                        selectedTab = 1
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
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it.uppercase(Locale.ROOT).take(12) },
                    singleLine = true,
                    label = { Text("Kode room") },
                    placeholder = { Text("Contoh: KA-1150") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = joinCode.trim().length >= 4,
                    onClick = {
                        showJoinDialog = false
                        joinCode = ""
                    },
                ) { Text("Gabung") }
            },
            dismissButton = { TextButton(onClick = { showJoinDialog = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun WatchRoomCard(room: WatchRoomUi, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(room.title, fontWeight = FontWeight.Bold)
                    Text("Host ${room.host}  •  ${room.members}/${room.capacity} orang", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onJoin) { Text("Join") }
            }
            if (room.genres.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(room.genres.joinToString("  •  "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun WatchTogetherEmptyState(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Watch Together", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
