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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class SocialDestination { HOME, FRIENDS, MESSAGES, NOTIFICATIONS, SEARCH_USERS, OTHER_PROFILE }

/**
 * Social V1 home and local destination router.
 *
 * The supplied Social blueprint defines the destinations and hierarchy while
 * KakaAnime visual language remains the UI/UX reference. Destination screens
 * are presentation-only until their backend services exist.
 */
@Composable
fun SocialScreen(onOpenWatchTogether: () -> Unit = {}) {
    var destination by remember { mutableStateOf(SocialDestination.HOME) }
    var selectedUser by remember { mutableStateOf(SearchUserUi("Hana", "@hana")) }

    when (destination) {
        SocialDestination.HOME -> SocialHomeScreen(
            onOpenWatchTogether = onOpenWatchTogether,
            onOpenFriends = { destination = SocialDestination.FRIENDS },
            onOpenMessages = { destination = SocialDestination.MESSAGES },
            onOpenNotifications = { destination = SocialDestination.NOTIFICATIONS },
            onOpenSearchUsers = { destination = SocialDestination.SEARCH_USERS },
        )
        SocialDestination.FRIENDS -> FriendsScreen(onBack = { destination = SocialDestination.HOME })
        SocialDestination.MESSAGES -> MessagesScreen(onBack = { destination = SocialDestination.HOME })
        SocialDestination.NOTIFICATIONS -> NotificationsScreen(onBack = { destination = SocialDestination.HOME })
        SocialDestination.SEARCH_USERS -> SearchUsersScreen(
            onBack = { destination = SocialDestination.HOME },
            onOpenProfile = { selectedUser = it; destination = SocialDestination.OTHER_PROFILE },
        )
        SocialDestination.OTHER_PROFILE -> OtherUserProfileScreen(
            user = selectedUser,
            onBack = { destination = SocialDestination.SEARCH_USERS },
            onMessage = { destination = SocialDestination.MESSAGES },
        )
    }
}

@Composable
private fun SocialHomeScreen(
    onOpenWatchTogether: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSearchUsers: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text("Social", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Find friends. Chat. Watch together.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { WatchTogetherHero(onClick = onOpenWatchTogether) }
        item {
            SocialSectionHeader("Active Friends")
            Spacer(Modifier.height(8.dp))
            ActiveFriendsRow()
        }
        item {
            SocialSectionHeader("Recent Messages")
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Kael", "Bro, episode barunya udah rilis?", "12:24", unread = 2, onClick = onOpenMessages)
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Hana", "Yuk nonton bareng nanti malam!", "11:03", unread = 1, onClick = onOpenMessages)
            Spacer(Modifier.height(8.dp))
            RecentMessageCard("Rynn", "Gila sih, episode kemarin keren.", "Yesterday", onClick = onOpenMessages)
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
            SocialActionCard("My Friends", "Teman yang sudah terhubung", onClick = onOpenFriends)
            Spacer(Modifier.height(8.dp))
            SocialActionCard("Friend Requests", "Lihat dan kelola permintaan teman", onClick = onOpenFriends)
            Spacer(Modifier.height(8.dp))
            SocialActionCard("Search Users", "Cari username atau ID", onClick = onOpenSearchUsers)
        }
        item {
            SocialSectionHeader("Notifications")
            Spacer(Modifier.height(8.dp))
            NotificationCard("Kael sent you a friend request", "10m ago", onClick = onOpenNotifications)
            Spacer(Modifier.height(8.dp))
            NotificationCard("Hana invited you to a Watch Together room", "30m ago", onClick = onOpenNotifications)
        }
        item { V2Placeholder("🌎", "Global Chat") }
        item { V2Placeholder("🎬", "Episode Chat") }
    }
}

@Composable
private fun WatchTogetherHero(onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) { Text("▶", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Watch Together", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Nonton anime bareng teman dalam satu room.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onClick) { Text("Create Room", fontWeight = FontWeight.Bold) }
                TextButton(onClick = onClick) { Text("Join Room", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable private fun SocialSectionHeader(title: String) = Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

@Composable
private fun ActiveFriendsRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("Shin" to "Watching", "Kael" to "Online", "Hana" to "In Room", "Mizu" to "Online").forEach { (name, status) ->
            Surface(Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(vertical = 14.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                    Spacer(Modifier.height(6.dp))
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RecentMessageCard(name: String, message: String, time: String, unread: Int = 0, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (unread > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) { Text(unread.toString(), Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable private fun ActivityCard(name: String, activity: String, time: String) = Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(name); Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold); Text(activity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable private fun SocialActionCard(title: String, subtitle: String, onClick: () -> Unit) = Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) }
}

@Composable private fun NotificationCard(message: String, time: String, onClick: () -> Unit) = Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Surface(Modifier.size(38.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }; Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(message, fontWeight = FontWeight.SemiBold); Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable private fun V2Placeholder(icon: String, title: String) = Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp)) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, style = MaterialTheme.typography.titleLarge); Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text("Coming Soon • V2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("V2", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
}

@Composable private fun Avatar(name: String) = Surface(Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
