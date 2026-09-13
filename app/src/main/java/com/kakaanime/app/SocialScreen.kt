package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Social V1 UI foundation.
 *
 * This screen intentionally establishes the KakaAnime visual hierarchy and
 * navigation entry points without introducing backend/social state yet.
 */
@Composable
fun SocialScreen() {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Social",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Find friends. Chat. Watch together.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SocialHeroCard(
                title = "Watch Together",
                subtitle = "Start or join a room and watch anime with friends.",
                actionLabel = "Open",
                onClick = { }
            )
        }

        item {
            SocialSectionHeader("Active Friends", "See All")
            Spacer(modifier = Modifier.height(8.dp))
            ActiveFriendsRow()
        }

        item {
            SocialSectionHeader("Recent Messages", "See All")
            Spacer(modifier = Modifier.height(8.dp))
            RecentMessageCard("Kael", "Bro, episode terbaru udah rilis?", "12:24")
            Spacer(modifier = Modifier.height(8.dp))
            RecentMessageCard("Hana", "Yuk nonton bareng nanti malam!", "11:03")
            Spacer(modifier = Modifier.height(8.dp))
            RecentMessageCard("Rynn", "Gila sih, episode kemarin keren.", "Yesterday")
        }

        item {
            SocialSectionHeader("Friends", "See All")
            Spacer(modifier = Modifier.height(8.dp))
            SocialActionCard("My Friends", "Teman yang sudah terhubung", onClick = { })
            Spacer(modifier = Modifier.height(8.dp))
            SocialActionCard("Friend Requests", "Lihat dan kelola permintaan teman", onClick = { })
            Spacer(modifier = Modifier.height(8.dp))
            SocialActionCard("Search Users", "Cari username atau ID", onClick = { })
        }

        item {
            SocialSectionHeader("Friend Activity", null)
            Spacer(modifier = Modifier.height(8.dp))
            ActivityCard("Shin", "Watching One Piece E1150", "10m ago")
            Spacer(modifier = Modifier.height(8.dp))
            ActivityCard("Kael", "Finished Solo Leveling", "2h ago")
            Spacer(modifier = Modifier.height(8.dp))
            ActivityCard("Hana", "Created a Watch Together room", "3h ago")
        }

        item {
            SocialSectionHeader("Notifications", "See All")
            Spacer(modifier = Modifier.height(8.dp))
            NotificationCard("Kael sent you a friend request", "10m ago")
            Spacer(modifier = Modifier.height(8.dp))
            NotificationCard("Hana invited you to a Watch Together room", "30m ago")
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(surfaceVariant),
                color = surfaceVariant,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌎", style = MaterialTheme.typography.titleLarge)
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("Global Chat", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Coming Soon • V2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("V2", color = primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                color = surfaceVariant,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎬", style = MaterialTheme.typography.titleLarge)
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("Episode Chat", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Coming Soon • V2",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("V2", color = primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SocialHeroCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎬", style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(actionLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SocialSectionHeader(title: String, action: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActiveFriendsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf("Shin", "Kael", "Rynn", "Mizu").forEach { name ->
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("●", color = MaterialTheme.colorScheme.primary)
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMessageCard(name: String, message: String, time: String) {
    SocialListCard(
        leading = "💬",
        title = name,
        subtitle = message,
        trailing = time
    )
}

@Composable
private fun ActivityCard(name: String, activity: String, time: String) {
    SocialListCard(
        leading = "●",
        title = name,
        subtitle = activity,
        trailing = time
    )
}

@Composable
private fun NotificationCard(message: String, time: String) {
    SocialListCard(
        leading = "🔔",
        title = message,
        subtitle = "",
        trailing = time
    )
}

@Composable
private fun SocialActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SocialListCard(leading: String, title: String, subtitle: String, trailing: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(leading, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
            Text(
                trailing,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
