package com.kakaanime.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Home V1 presentation entry point. Data loading remains outside the UI layer. */
@Composable
fun HomeV1Screen(
    state: HomeUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onAnimeClick: (HomeAnimeUi) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onDiamondClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    onWatchTogetherClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp)
    ) {
        Text("KakaAnime")
        if (state.anime.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        } else {
            state.anime.forEach { anime ->
                Text(
                    text = anime.title,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
