package com.kakaanime.app.ui.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SocialScreen(
    state: SocialUiState,
    onProfileClick: (String) -> Unit = {},
    onWatchTogetherClick: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("Social", fontSize = 25.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                Text("Nonton dan ngobrol bareng komunitas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SocialModeCard("GLOBAL", "${state.globalOnlineCount} online", Icons.Outlined.Public, Modifier.weight(1f))
                SocialModeCard("ANIME", "${state.animeRoomCount} room", Icons.Outlined.Groups, Modifier.weight(1f))
            }
        }
        item {
            Surface(
                onClick = onWatchTogetherClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(48.dp), CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.VideoCall, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("NONTON BARENG", fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        Text("Buat atau gabung room dan tonton bareng.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", fontSize = 25.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Text("Active Friends", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold) }
        if (state.friends.isEmpty()) {
            item {
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)) {
                    Text("Belum ada teman yang sedang online.", Modifier.padding(18.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.friends, key = { it.id }) { friend ->
                Row(
                    Modifier.fillMaxWidth().clickable { onProfileClick(friend.id) }.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(Modifier.size(46.dp), CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text(friend.username.take(2).uppercase(), fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(friend.username, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(friend.status, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialModeCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(modifier, RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
