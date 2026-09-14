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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class FriendUi(val name: String, val status: String, val watching: String? = null)
private data class FriendRequestUi(val name: String, val mutual: Int)

@Composable
fun FriendsScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val friends = remember { listOf(FriendUi("Kael", "Watching", "One Piece • Episode 1140"), FriendUi("Hana", "Online"), FriendUi("Mizu", "Watching", "Frieren • Episode 20"), FriendUi("Rin", "Offline")) }
    val requests = remember { listOf(FriendRequestUi("Aki", 3), FriendRequestUi("Yuna", 1)) }
    val visibleFriends = friends.filter { it.name.contains(searchQuery, true) }
    val visibleRequests = requests.filter { it.name.contains(searchQuery, true) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Friends", Modifier.weight(1f).padding(start = 4.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showSearch = !showSearch }) { Icon(Icons.Default.Search, "Search users") }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Manage your friends and requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                if (showSearch) {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search users...") }, shape = RoundedCornerShape(18.dp))
                    Spacer(Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("My Friends") })
                    FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Requests ${requests.size}") })
                }
            }
            if (selectedTab == 0) {
                item { FriendSectionHeader("My Friends", "${visibleFriends.size} friends") }
                items(visibleFriends, key = { it.name }) { FriendCard(it) }
            } else {
                item { FriendSectionHeader("Requests", "Friend requests") }
                items(visibleRequests, key = { it.name }) { FriendRequestCard(it) }
            }
            item {
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = RoundedCornerShape(16.dp)) {
                    Text("Friend actions and search will connect to the Social backend after the UI foundation is complete.", Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable private fun FriendSectionHeader(title: String, subtitle: String) = Column(Modifier.padding(top = 4.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

@Composable private fun FriendCard(friend: FriendUi) = Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(friend.name)
        Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(friend.name, fontWeight = FontWeight.SemiBold); Text(friend.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); friend.watching?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } }
        OutlinedButton(onClick = { }) { Text("Message") }
    }
}

@Composable private fun FriendRequestCard(request: FriendRequestUi) = Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(request.name)
        Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(request.name, fontWeight = FontWeight.SemiBold); Text("${request.mutual} mutual friends", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Column(horizontalAlignment = Alignment.End) { Button(onClick = { }) { Text("Accept") }; OutlinedButton(onClick = { }) { Text("Decline") } }
    }
}

@Composable private fun Avatar(name: String) = Surface(Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }