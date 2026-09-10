package com.kakaanime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.player.VideoPlayerScreen

data class Anime(
    val title: String,
    val episode: String,
    val genre: String,
    val description: String,
    val introStart: Long = 0L,
    val introEnd: Long = 0L,
    val outroStart: Long = 0L,
    val outroEnd: Long = 0L
)

private val localAnime = listOf(
    Anime("One Piece", "Episode 1140", "Action, Adventure", "Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka.", 90L, 180L, 1380L, 1440L),
    Anime("Solo Leveling", "Episode 25", "Action, Fantasy", "Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.", 75L, 165L, 1380L, 1440L)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                KakaAnimeApp()
            }
        }
    }
}

@Composable
fun KakaAnimeApp() {
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var showPlayer by remember { mutableStateOf(false) }

    if (showPlayer && selectedAnime != null) {
        VideoPlayerScreen(
            videoUrl = "https://media.w3.org/2010/05/bunny/trailer.mp4",
            introStart = selectedAnime!!.introStart,
            introEnd = selectedAnime!!.introEnd,
            outroStart = selectedAnime!!.outroStart,
            outroEnd = selectedAnime!!.outroEnd,
            modifier = Modifier.fillMaxSize()
        )
    } else if (selectedAnime == null) {
        HomeScreen(
            animeList = localAnime,
            onAnimeClick = { selectedAnime = it }
        )
    } else {
        AnimeDetailScreen(
            anime = selectedAnime!!,
            onBack = { selectedAnime = null },
            onWatchClick = { showPlayer = true }
        )
    }
}

@Composable
fun HomeScreen(
    animeList: List<Anime>,
    onAnimeClick: (Anime) -> Unit
) {
    var search by remember { mutableStateOf("") }

    val filteredAnime = animeList.filter {
        it.title.contains(search, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .padding(16.dp)
    ) {
        Text(
            text = "KakaAnime",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari anime...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredAnime) { anime ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnimeClick(anime) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = anime.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = anime.episode)
                        Text(text = anime.genre)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeDetailScreen(
    anime: Anime,
    onBack: () -> Unit,
    onWatchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Kembali")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = anime.title,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = anime.genre,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = anime.description,
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onWatchClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("▶  ${anime.episode}")
        }
    }
}
