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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Social V1 home foundation.
 *
 * Watch Together is the only live navigation action on this screen until the
 * remaining Social destinations have real feature screens behind them.
 * Preview content is intentionally presentation-only; it is not backend data.
 */
@Composable
fun SocialScreen(
    onOpenWatchTogether: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Social",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Find friends. Chat. Watch together.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            WatchTogetherHero(onClick = onOpenWatchTogether)
        }

        item {
            SocialSectionHeader("Active Friends")
            Spacer(Modifier.height(8.dp))
            ActiveFriendsRow()
        }

        item {
            SocialSectionHeader("Recent Messages")
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Kael", "Bro, episode barunya udah rilis?", "12:24", unread = 2)
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Hana", "Yuk nonton bareng nanti malam!", "11:03", unread = 1)
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Rynn", "Gila sih, episode kemarin keren.", "Yesterday")
        }

        item {
            SocialSectionHeader("Friend Activity")
            Spacer(Modifier.height(8.dp))
            ActivityCard("Mizu", "Finished Solo Leveling EP 8", "2h ago")
            Spacer(Modifier.height(8.dp))
            ActivityCard("Hana", "Created a Watch Together room", "3h ago")
        }

        item {
            SocialSectionHeader("Friends")
            Spacer(Modifier.height(8.dp))
            SocialActionCard("My Friends", "Teman yang sudah terhubung")
            Spacer(Modifier.height(8.dp))
            SocialActionCard("Friend Requests", "Lihat dan kelola permintaan teman")
            Spacer(Modifier.height(8.dp))
            SocialActionCard("Search Users", "Cari username atau ID")
        }

        item {
            SocialSectionHeader("Notifications")
            Spacer(Modifier.height(8.dp))
            NotificationCard("Kael sent you a friend request", "10m ago")
            Spacer(Modifier.height(8.dp))
            NotificationCard("Hana invited you to a Watch Together room", "30m ago")
        }

        item { V2Placeholder("🌎", "Global Chat") }
        item { V2Placeholder("🎬", "Episode Chat") }
    }
}

@Composable
private fun WatchTogetherHero(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("▶", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Watch Together", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Nonton anime bareng teman dalam satu room.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onClick) {
                    Text("Create Room", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onClick) {
                    Text("Join Room", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SocialSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ActiveFriendsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf("Shin" to "Watching", "Kael" to "Online", "Hana" to "In Room", "Mizu" to "Online").forEach { (name, status) ->
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RecentMessageCard(name: String, message: String, time: String, unread: Int = 0) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (unread > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            unread.toString(),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(name: String, activity: String, time: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text(activity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SocialActionCard(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NotificationCard(message: String, time: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) { Text("!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(message, fontWeight = FontWeight.SemiBold)
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun V2Placeholder(icon: String, title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text("Coming Soon • V2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("V2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
