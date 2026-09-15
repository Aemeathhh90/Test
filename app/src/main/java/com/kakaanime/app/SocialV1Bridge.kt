package com.kakaanime.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kakaanime.app.ui.profile.OtherUserProfileScreen
import com.kakaanime.app.ui.profile.ProfileUiState
import com.kakaanime.app.ui.social.SocialFriendUi
import com.kakaanime.app.ui.social.SocialScreen as SocialV1Presentation
import com.kakaanime.app.ui.social.SocialUiState

/** Compatibility boundary: Social V1 presentation is UI-only; existing social/business flows stay isolated. */
@Composable
fun SocialV1Screen(onOpenWatchTogether: () -> Unit = {}) {
    var selectedProfile by remember { mutableStateOf<String?>(null) }
    val friends = remember {
        listOf(
            SocialFriendUi("kael", "Kael", "Online"),
            SocialFriendUi("hana", "Hana", "In Room"),
            SocialFriendUi("mizu", "Mizu", "Watching"),
        )
    }

    val profileId = selectedProfile
    if (profileId != null) {
        val friend = friends.firstOrNull { it.id == profileId }
        if (friend != null) {
            OtherUserProfileScreen(
                state = ProfileUiState(
                    userId = friend.id,
                    username = friend.username,
                    bio = "Anime lover • KakaAnime",
                    status = friend.status,
                    watchingTitles = if (friend.status == "Watching") listOf("Solo Leveling") else emptyList(),
                    watchedCount = 24,
                    favoriteCount = 8,
                    followingCount = 16,
                    isSelf = false,
                    isFollowing = false,
                ),
                onBack = { selectedProfile = null },
            )
        } else {
            selectedProfile = null
        }
        return
    }

    SocialV1Presentation(
        state = SocialUiState(
            username = "Shin",
            friends = friends,
            globalOnlineCount = 128,
            animeRoomCount = 12,
        ),
        onProfileClick = { selectedProfile = it },
        onWatchTogetherClick = onOpenWatchTogether,
    )
}
