package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.data.KakaAnimePreferences

private val profileAvatarColors = listOf(
    Color(0xFFFF4D67),
    Color(0xFF9C6BFF),
    Color(0xFF20C8E8),
    Color(0xFF35C98A)
)

private val profileBannerGradients = listOf(
    listOf(Color(0xFF0B1018), Color(0xFFFF4D67)),
    listOf(Color(0xFF171125), Color(0xFF9C6BFF)),
    listOf(Color(0xFF071A22), Color(0xFF20C8E8)),
    listOf(Color(0xFF101A16), Color(0xFF35C98A))
)

@Composable
fun EditProfileScreen(
    preferences: KakaAnimePreferences,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(preferences.loadProfileName()) }
    var bio by remember { mutableStateOf(preferences.loadProfileBio()) }
    var avatarIndex by remember { mutableStateOf(preferences.loadProfileAvatarIndex()) }
    var bannerIndex by remember { mutableStateOf(preferences.loadProfileBannerIndex()) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali")
            }
            Column(Modifier.weight(1f)) {
                Text("Edit Profile", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Customize your profile", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    preferences.saveProfileName(name)
                    preferences.saveProfileBio(bio)
                    preferences.saveProfileAvatarIndex(avatarIndex)
                    preferences.saveProfileBannerIndex(bannerIndex)
                    onBack()
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(5.dp))
                Text("Save")
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp)).background(
                    Brush.linearGradient(profileBannerGradients[bannerIndex])
                )
            ) {
                Text(
                    "KakaAnime",
                    Modifier.align(Alignment.Center).padding(top = 8.dp),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "ANIME ALWAYS WITH YOU",
                    Modifier.align(Alignment.Center).padding(top = 55.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = .72f)
                )
                Surface(
                    Modifier.align(Alignment.BottomStart).padding(14.dp),
                    CircleShape,
                    color = profileAvatarColors[avatarIndex]
                ) {
                    Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                        Text(
                            name.trim().take(2).ifBlank { "KA" }.uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    Modifier.align(Alignment.BottomStart).padding(start = 76.dp, bottom = 10.dp),
                    CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .92f)
                ) {
                    Icon(Icons.Outlined.Edit, "Edit avatar", Modifier.padding(9.dp).size(17.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Profile Information", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Username") },
                    supportingText = { Text("${name.length}/20") },
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    label = { Text("Bio") },
                    supportingText = { Text("${bio.length}/100") },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Avatar", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    profileAvatarColors.forEachIndexed { index, color ->
                        Surface(
                            Modifier.size(66.dp).clickable { avatarIndex = index },
                            CircleShape,
                            color = color.copy(alpha = if (avatarIndex == index) 1f else .62f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    name.trim().take(2).ifBlank { "KA" }.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Banner", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    profileBannerGradients.forEachIndexed { index, gradient ->
                        Box(
                            Modifier.size(width = 132.dp, height = 74.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(gradient))
                                .clickable { bannerIndex = index }
                        ) {
                            if (bannerIndex == index) {
                                Surface(
                                    Modifier.align(Alignment.TopEnd).padding(7.dp),
                                    CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = .88f)
                                ) {
                                    Icon(Icons.Outlined.Check, null, Modifier.padding(5.dp).size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
