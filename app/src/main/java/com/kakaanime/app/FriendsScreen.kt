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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
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

private data class FriendUi(
    val name: String,
    val status: String,
    val watching: String? = null,
)

private data class FriendRequestUi(
    val name: String,
    val mutual: Int,
)

/**
 * Friends UI foundation.
 * Local presentation state only; real friend requests/search require Social backend.
 */
@Composable
fun FriendsScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val friends = remember {
        listOf(
            FriendUi("Kael", "Watching", "One Piece • Episode 1140"),
            FriendUi("Hana", "Online"),
            FriendUi("Mizu", "Watching", "Frieren • Episode 20"),
            FriendUi("Rin", "Offline"),
        )
    }
    val requests = remember {
        listOf(
            FriendRequestUi("Aki", 3),
            FriendRequestUi("Yuna", 1),
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("‹  Kembali") }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Text("⌕", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            item {
                Column {
                    Text("Friends", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Kelola teman dan lihat siapa yang sedang aktif.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showSearch) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search Users") },
                        placeholder = { Text("Cari username") },
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("My Friends") })
                    FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Requests ${requests.size}") })
                }
            }

            if (selectedTab == 0) {
                item {
                    FriendSectionHeader("My Friends", "${friends.size} teman")
                }
                items(friends, key = { it.name }) { friend ->
                    FriendCard(friend)
                }
            } else {
                item {
                    FriendSectionHeader("Requests", "Permintaan pertemanan")
                }
                items(requests, key = { it.name }) { request ->
                    FriendRequestCard(request)
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Friend list, request, dan pencarian user akan terhubung ke Social backend setelah fondasi UI selesai.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendSectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(top = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FriendCard(friend: FriendUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(friend.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(friend.name, fontWeight = FontWeight.SemiBold)
                Text(friend.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                friend.watching?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            OutlinedButton(onClick = { }) { Text("Message") }
        }
    }
}

@Composable
private fun FriendRequestCard(request: FriendRequestUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(request.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(request.name, fontWeight = FontWeight.SemiBold)
                Text("${request.mutual} mutual friends", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { }) { Text("Accept") }
                OutlinedButton(onClick = { }) { Text("Decline") }
            }
        }
    }
}
