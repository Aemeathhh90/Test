package com.kakaanime.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class NotificationUi(val message: String, val time: String, val type: String)

@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    val notifications = listOf(
        NotificationUi("Kael sent you a friend request", "2m ago", "Friends"),
        NotificationUi("Hana invited you to a Watch Together room", "10m ago", "Watch Together"),
        NotificationUi("Rynn mentioned you in a message", "1h ago", "Messages"),
        NotificationUi("Mizu started watching Solo Leveling EP 12", "2h ago", "Friends"),
        NotificationUi("Zed accepted your friend request", "5h ago", "Friends"),
        NotificationUi("New episode available: One Piece EP1150", "1d ago", "System"),
    )

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(notifications) { NotificationItem(it) }
        }
    }
}

@Composable
private fun NotificationItem(item: NotificationUi) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(item.type.take(1), fontWeight = FontWeight.Bold) }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(item.message, fontWeight = FontWeight.SemiBold)
                Text(item.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
