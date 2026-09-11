package com.kakaanime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.ui.theme.KakaAnimeTheme
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.rememberKakaThemeState

data class Anime(
    val title: String,
    val latestEpisode: Int,
    val genre: String,
    val description: String,
    val studio: String,
    val season: String,
    val year: String,
    val type: String,
    val status: String,
    val rating: String,
    val introStart: Long = 0L,
    val introEnd: Long = 0L,
    val outroStart: Long = 0L,
    val outroEnd: Long = 0L
)

private val localAnime = listOf(
    Anime(
        title = "One Piece",
        latestEpisode = 1140,
        genre = "Action, Adventure, Fantasy",
        description = "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka menuju One Piece.",
        studio = "Toei Animation",
        season = "Ongoing",
        year = "1999",
        type = "TV",
        status = "Ongoing",
        rating = "9.0",
        introStart = 90L,
        introEnd = 180L,
        outroStart = 1380L,
        outroEnd = 1440L
    ),
    Anime(
        title = "Solo Leveling",
        latestEpisode = 25,
        genre = "Action, Fantasy",
        description = "Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.",
        studio = "A-1 Pictures",
        season = "Season 2",
        year = "2025",
        type = "TV",
        status = "Finished",
        rating = "8.8",
        introStart = 75L,
        introEnd = 165L,
        outroStart = 1380L,
        outroEnd = 1440L
    )
)

private enum class AnimeScreen {
    HOME,
    DETAIL,
    PLAYER
}

private enum class BottomTab(
    val label: String,
    val symbol: String
) {
    HOME("Home", "⌂"),
    CALENDAR("Calendar", "□"),
    HISTORY("History", "◷"),
    FAVORITE("Favorite", "♥"),
    PROFILE("Profile", "●")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeState = rememberKakaThemeState()

            KakaAnimeTheme(
                themeState = themeState
            ) {
                KakaAnimeApp()
            }
        }
    }
}

@Composable
fun KakaAnimeApp() {

    val themeState = rememberKakaThemeState()

    var selectedAnime by remember {
        mutableStateOf<Anime?>(null)
    }

    var selectedEpisode by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedTab by remember {
        mutableStateOf(BottomTab.HOME)
    }

    val currentScreen = when {
        selectedAnime != null && selectedEpisode != null ->
            AnimeScreen.PLAYER

        selectedAnime != null ->
            AnimeScreen.DETAIL

        else ->
            AnimeScreen.HOME
    }

    KakaAnimeTheme(
        themeState = themeState
    ) {

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {

                val forward =
                    targetState.ordinal > initialState.ordinal

                val direction =
                    if (forward) 1 else -1

                (
                    androidx.compose.animation.fadeIn(
                        animationSpec =
                            androidx.compose.animation.core.tween(
                                durationMillis = 260
                            )
                    ) +
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = {
                            direction * (it / 5)
                        },
                        animationSpec =
                            androidx.compose.animation.core.tween(
                                durationMillis = 360,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            )
                    )
                ) togetherWith (
                    androidx.compose.animation.fadeOut(
                        animationSpec =
                            androidx.compose.animation.core.tween(
                                durationMillis = 180
                            )
                    ) +
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = {
                            -direction * (it / 7)
                        },
                        animationSpec =
                            androidx.compose.animation.core.tween(
                                durationMillis = 300,
                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                            )
                    )
                )
            },
            label = "kakaanime_page_transition"
        ) { screen ->

            when (screen) {

                AnimeScreen.HOME -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background
                            )
                    ) {

                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {

                                val direction =
                                    if (
                                        targetState.ordinal >=
                                        initialState.ordinal
                                    ) {
                                        1
                                    } else {
                                        -1
                                    }

                                (
                                    androidx.compose.animation.fadeIn(
                                        animationSpec =
                                            androidx.compose.animation.core.tween(
                                                durationMillis = 220
                                            )
                                    ) +
                                    androidx.compose.animation.slideInHorizontally(
                                        initialOffsetX = {
                                            direction * (it / 8)
                                        },
                                        animationSpec =
                                            androidx.compose.animation.core.tween(
                                                durationMillis = 280
                                            )
                                    )
                                ) togetherWith (
                                    androidx.compose.animation.fadeOut(
                                        animationSpec =
                                            androidx.compose.animation.core.tween(
                                                durationMillis = 160
                                            )
                                    ) +
                                    androidx.compose.animation.slideOutHorizontally(
                                        targetOffsetX = {
                                            -direction * (it / 8)
                                        },
                                        animationSpec =
                                            androidx.compose.animation.core.tween(
                                                durationMillis = 240
                                            )
                                    )
                                )
                            },
                            label = "bottom_tab_transition",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 92.dp)
                        ) { tab ->

                            when (tab) {

                                BottomTab.HOME -> {

                                    HomeScreen(
                                        animeList = localAnime,
                                        onAnimeClick = {
                                            selectedAnime = it
                                        }
                                    )
                                }

                                BottomTab.CALENDAR -> {
                                    CalendarScreen()
                                }

                                BottomTab.HISTORY -> {
                                    HistoryScreen()
                                }

                                BottomTab.FAVORITE -> {
                                    FavoriteScreen()
                                }

                                BottomTab.PROFILE -> {

                                    ProfileScreen(
                                        themeState = themeState
                                    )
                                }
                            }
                        }

                        KakaBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 16.dp
                                )
                        )
                    }
                }

                AnimeScreen.DETAIL -> {

                    if (selectedAnime != null) {

                        AnimeDetailScreen(
                            anime = selectedAnime!!,
                            onBack = {
                                selectedAnime = null
                                selectedEpisode = null
                            },
                            onEpisodeClick = { episode ->

                                selectedEpisode = episode
                            }
                        )
                    }
                }

                AnimeScreen.PLAYER -> {

                    if (
                        selectedAnime != null &&
                        selectedEpisode != null
                    ) {

                        VideoPlayerScreen(
                            videoUrl =
                                "https://media.w3.org/2010/05/bunny/trailer.mp4",

                            introStart =
                                selectedAnime!!.introStart,

                            introEnd =
                                selectedAnime!!.introEnd,

                            outroStart =
                                selectedAnime!!.outroStart,

                            outroEnd =
                                selectedAnime!!.outroEnd,

                            modifier =
                                Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun KakaBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 7.dp,
                    vertical = 7.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomTab.entries.forEach { tab ->

                KakaBottomItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = {
                        onTabSelected(tab)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KakaBottomItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val indicatorWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (selected) 58.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 500f
        ),
        label = "indicator_width"
    )

    val iconSize by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (selected) 27.dp else 23.dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 500f
        ),
        label = "icon_size"
    )

    Column(
        modifier = modifier
            .clickable {
                onClick()
            }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .height(30.dp)
                .width(68.dp),
            contentAlignment = Alignment.Center
        ) {

            if (selected) {

                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(30.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.18f
                            ),
                            RoundedCornerShape(18.dp)
                        )
                )
            }

            Text(
                text = tab.symbol,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = iconSize.value.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }

        Text(
            text = tab.label,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 10.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
fun HomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit
) {

    var search by remember {
        mutableStateOf("")
    }

    val filteredAnime = animeList.filter {
        it.title.contains(search, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 112.dp
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "KakaAnime",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Temukan anime favoritmu",
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.55f
                        ),
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.16f
                            ),
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "KA",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Cari anime, genre, karakter...")
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (search.isEmpty()) {

            item {

                val featured = animeList.firstOrNull()

                if (featured != null) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable {
                                onAnimeClick(featured)
                            }
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Box(
                                modifier = Modifier
                                    .width(125.dp)
                                    .height(178.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "POSTER",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier.width(16.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "FEATURED",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )

                                Text(
                                    text = featured.title,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )

                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )

                                Text(
                                    text = "⭐ ${featured.rating}  •  ${featured.status}",
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.7f
                                    ),
                                    fontSize = 12.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )

                                Text(
                                    text = featured.genre,
                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.6f
                                    ),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Button(
                                    onClick = {
                                        onAnimeClick(featured)
                                    },
                                    shape = RoundedCornerShape(13.dp)
                                ) {

                                    Text(
                                        text = "▶  Mulai Nonton"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {

                SectionTitle("📺 Continue Watching")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AnimeHorizontalRow(
                    animeList = animeList,
                    onAnimeClick = onAnimeClick
                )
            }

            item {

                SectionTitle("🆕 New Updates")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AnimeHorizontalRow(
                    animeList = animeList.reversed(),
                    onAnimeClick = onAnimeClick
                )
            }

            item {

                SectionTitle("🔥 Trending")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AnimeHorizontalRow(
                    animeList = animeList,
                    onAnimeClick = onAnimeClick
                )
            }

            item {

                SectionTitle("🕘 Recently Updated")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AnimeHorizontalRow(
                    animeList = animeList.reversed(),
                    onAnimeClick = onAnimeClick
                )
            }

            item {

                SectionTitle("📅 Season")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {

                    SeasonButton("‹ Previous")

                    SeasonButton(
                        text = "This Season",
                        active = true
                    )

                    SeasonButton("Next ›")
                }
            }

            item {

                SectionTitle("⚡ Quick Access")

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    QuickCard(
                        title = "Genre",
                        symbol = "◈",
                        modifier = Modifier.weight(1f)
                    )

                    QuickCard(
                        title = "Calendar",
                        symbol = "□",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

        } else {

            item {

                SectionTitle("🔎 Hasil Pencarian")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            if (filteredAnime.isEmpty()) {

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Anime tidak ditemukan",
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.6f
                            )
                        )
                    }
                }

            } else {

                items(filteredAnime) { anime ->

                    AnimeListCard(
                        anime = anime,
                        onClick = {
                            onAnimeClick(anime)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeHorizontalRow(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit
) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(animeList) { anime ->

            Column(
                modifier = Modifier
                    .width(145.dp)
                    .clickable {
                        onAnimeClick(anime)
                    }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "POSTER",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(9.dp)
                )

                Text(
                    text = anime.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "Ep ${anime.latestEpisode}  •  ⭐ ${anime.rating}",
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.55f
                    ),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AnimeListCard(
    anime: Anime,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(96.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "POSTER",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = anime.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = anime.genre,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Text(
                    text = "Episode ${anime.latestEpisode}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "⭐ ${anime.rating}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarScreen() {

    PlaceholderScreen(
        title = "Calendar",
        subtitle = "Jadwal anime berdasarkan hari dan tanggal."
    )
}

@Composable
private fun HistoryScreen() {

    PlaceholderScreen(
        title = "History",
        subtitle = "Anime dan episode yang terakhir kamu tonton."
    )
}

@Composable
private fun FavoriteScreen() {

    PlaceholderScreen(
        title = "Favorite",
        subtitle = "Anime favorit kamu akan muncul di sini."
    )
}

@Composable
private fun ProfileScreen(
    themeState: com.kakaanime.app.ui.theme.KakaThemeState
) {

    var showAppearance by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 24.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                text = "Profile",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Kelola akun dan tampilan KakaAnime",
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.6f
                )
            )
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.16f
                                ),
                                RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "KA",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column {

                        Text(
                            text = "KakaAnime User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text(
                            text = "Free Account",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showAppearance = !showAppearance
                    },
                shape = RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {

                            Text(
                                text = "🎨 Appearance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            Text(
                                text = "Tema dan warna KakaAnime",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = if (showAppearance) "▲" else "▼",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showAppearance) {

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        Text(
                            text = "Mode",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            AppearanceButton(
                                text = "Dark",
                                selected = themeState.darkMode,
                                onClick = {
                                    themeState.darkMode = true
                                }
                            )

                            AppearanceButton(
                                text = "Light",
                                selected = !themeState.darkMode,
                                onClick = {
                                    themeState.darkMode = false
                                }
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        Text(
                            text = "Accent Color",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        KakaAccent.entries.forEach { accent ->

                            val premium =
                                accent != KakaAccent.Blue

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!premium) {
                                            themeState.accent = accent
                                        }
                                    }
                                    .padding(
                                        vertical = 9.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            accent.primary,
                                            RoundedCornerShape(50)
                                        )
                                )

                                Spacer(
                                    modifier = Modifier.width(12.dp)
                                )

                                Text(
                                    text = accent.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (
                                        themeState.accent == accent
                                    ) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )

                                if (premium) {

                                    Text(
                                        text = "PREMIUM",
                                        color = MaterialTheme
                                            .colorScheme
                                            .primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (
                                    themeState.accent == accent
                                ) {

                                    Text(
                                        text = "✓",
                                        color = MaterialTheme
                                            .colorScheme
                                            .primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "🔒 Warna Premium akan terbuka setelah akun Premium aktif.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "💎 KakaAnime Premium",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Buka fitur Premium, termasuk custom accent color.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lihat Premium")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 10.dp
            ),
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.65f
            )
        )
    }
}

@Composable
fun AnimeDetailScreen(
    anime: Anime,
    onBack: () -> Unit,
    onEpisodeClick: (Int) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {

        item {

            TextButton(
                onClick = onBack
            ) {
                Text("← Kembali")
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = anime.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "⭐ ${anime.rating}  •  ${anime.status}",
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.65f
                )
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = anime.genre,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.65f
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {
                    onEpisodeClick(anime.latestEpisode)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "▶  Tonton Episode ${anime.latestEpisode}"
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            SectionTitle("📖 Sinopsis")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = anime.description,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            SectionTitle("ℹ️ Info Anime")

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            InfoRow("Studio", anime.studio)
            InfoRow("Season", anime.season)
            InfoRow("Tahun", anime.year)
            InfoRow("Type", anime.type)
            InfoRow("Status", anime.status)

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            SectionTitle("🆕 Episode Baru")

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            text = "Episode ${anime.latestEpisode}",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "Episode terbaru tersedia",
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "BARU",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            SectionTitle("🎬 Episode")

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        items(
            (
                anime.latestEpisode downTo
                    maxOf(
                        1,
                        anime.latestEpisode - 19
                    )
            ).toList()
        ) { episode ->

            EpisodeItem(
                episode = episode,
                isNew = episode == anime.latestEpisode,
                onClick = {
                    onEpisodeClick(episode)
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.55f
            )
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EpisodeItem(
    episode: Int,
    isNew: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Episode $episode",
                fontWeight = FontWeight.Medium
            )

            if (isNew) {

                Text(
                    text = "BARU",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun SeasonButton(
    text: String,
    active: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 11.dp
            ),
            color = if (active) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (active) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun QuickCard(
    title: String,
    symbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = symbol,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 25.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
