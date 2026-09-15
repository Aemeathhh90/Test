package com.kakaanime.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kakaanime.app.data.AniListMetadataService
import com.kakaanime.app.data.AnimeStateIdentity
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.SeasonAwareWatchHistoryEntry
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.network.AnimeRepository
import com.kakaanime.app.ui.home.ContinueWatchingUi
import com.kakaanime.app.ui.home.HomeAnimeUi
import com.kakaanime.app.ui.home.HomeUiState
import com.kakaanime.app.ui.home.HomeV1Screen

/**
 * Integration boundary for Home V1.
 * Network/backend and persistence stay in the app layer; the presentation
 * screen receives only UI-owned models.
 */
@Composable
fun HomeV1IntegrationScreen(
    fallbackAnime: List<Anime>,
    monetizationState: MonetizationState,
    refreshKey: Int,
    preferences: KakaAnimePreferences,
    onCatalogLoaded: (List<Anime>) -> Unit,
    onAnimeClick: (Anime) -> Unit,
    onContinueWatchingClick: (Anime, Int) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDiamondClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onWatchTogetherClick: () -> Unit,
) {
    val metadataService = remember { AniListMetadataService() }
    var catalog by remember(fallbackAnime) { mutableStateOf(fallbackAnime) }
    var posterUrls by remember(fallbackAnime) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var watchHistory by remember(preferences) { mutableStateOf(preferences.loadWatchHistorySeasonAware()) }

    LaunchedEffect(fallbackAnime, refreshKey) {
        val loaded = AnimeRepository.loadAnime(fallbackAnime)
        catalog = loaded
        onCatalogLoaded(loaded)
        posterUrls = buildMap {
            loaded.forEach { anime ->
                metadataService.findByTitle(anime.title)?.imageUrl?.let { put(anime.title, it) }
            }
        }
        watchHistory = preferences.loadWatchHistorySeasonAware()
    }

    val historyBySeason = watchHistory
        .groupBy { it.animeGroupId to it.seasonNumber }
        .values
        .mapNotNull { entries -> entries.maxByOrNull { it.watchedAt } }

    val continueWatching = historyBySeason.mapNotNull { history ->
        catalog.firstOrNull { anime ->
            val identity = AnimeStateIdentity(
                anime.animeGroupId.ifBlank { anime.title },
                anime.seasonNumber,
                anime.seasonTitle,
            )
            identity.animeGroupId == history.animeGroupId &&
                identity.seasonNumber == history.seasonNumber &&
                (identity.seasonTitle?.trim()?.equals(history.seasonTitle?.trim(), true)
                    ?: history.seasonTitle.isNullOrBlank())
        }?.let { anime ->
            ContinueWatchingUi(
                anime = anime.toHomeAnimeUi(posterUrls[anime.title]),
                episode = history.episode,
                episodeTitle = history.episodeTitle,
                thumbnailUrl = history.episodeThumbnailUrl,
                progressPercent = progressPercent(history),
            ) to anime
        }
    }

    val state = HomeUiState(
        anime = catalog.mapIndexed { index, anime ->
            anime.toHomeAnimeUi(
                posterUrl = posterUrls[anime.title],
                isNew = index < 2,
            )
        },
        continueWatching = continueWatching.map { it.first },
        diamonds = monetizationState.diamonds,
        isPremium = monetizationState.isPremium,
        username = "Akun Saya",
    )

    HomeV1Screen(
        state = state,
        onAnimeClick = { ui ->
            catalog.firstOrNull { it.homeIdentity() == ui.id || it.title == ui.title }?.let(onAnimeClick)
        },
        onContinueWatchingClick = { ui ->
            continueWatching.firstOrNull { it.first.anime.id == ui.anime.id && it.first.episode == ui.episode }
                ?.let { onContinueWatchingClick(it.second, it.first.episode) }
        },
        onProfileClick = onProfileClick,
        onNotificationsClick = onNotificationsClick,
        onDiamondClick = onDiamondClick,
        onPremiumClick = onPremiumClick,
        onWatchTogetherClick = onWatchTogetherClick,
    )
}

private fun Anime.toHomeAnimeUi(
    posterUrl: String?,
    isNew: Boolean = false,
): HomeAnimeUi = HomeAnimeUi(
    id = homeIdentity(),
    title = title,
    latestEpisode = latestEpisode,
    rating = rating.ifBlank { "-" },
    genre = genre,
    posterUrl = posterUrl,
    status = status.ifBlank { "Ongoing" },
    isNew = isNew,
)

private fun Anime.homeIdentity(): String = animeGroupId.ifBlank { title }

private fun progressPercent(history: SeasonAwareWatchHistoryEntry): Int {
    val duration = history.durationMs ?: return 0
    if (duration <= 0L) return 0
    return (history.watchedAt.takeIf { it >= 0L }?.let { 0 } ?: 0).coerceIn(0, 100)
}
