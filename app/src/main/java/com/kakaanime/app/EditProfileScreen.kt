package com.kakaanime.app

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.kakaanime.app.data.KakaAnimePreferences

private val profileAvatarColors = listOf(Color(0xFFFF4D67), Color(0xFF9C6BFF), Color(0xFF20C8E8), Color(0xFF35C98A))
private val profileBannerGradients = listOf(
    listOf(Color(0xFF0B1018), Color(0xFFFF4D67)), listOf(Color(0xFF171125), Color(0xFF9C6BFF)),
    listOf(Color(0xFF071A22), Color(0xFF20C8E8)), listOf(Color(0xFF101A16), Color(0xFF35C98A))
)

@Composable
fun EditProfileScreen(preferences: KakaAnimePreferences, onBack: () -> Unit, isPremium: Boolean = false, onPremiumClick: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(preferences.loadProfileName()) }
    var bio by remember { mutableStateOf(preferences.loadProfileBio()) }
    var avatarIndex by remember { mutableStateOf(preferences.loadProfileAvatarIndex()) }
    var profilePhotoUri by remember { mutableStateOf(preferences.loadProfilePhotoUri()) }
    var bannerUri by remember { mutableStateOf(preferences.loadProfileBannerUri()) }
    var premiumBannerUri by remember { mutableStateOf(preferences.loadPremiumBannerUri()) }
    var animatedProfileUri by remember { mutableStateOf(preferences.loadAnimatedProfileUri()) }
    val gifImageLoader = remember { ImageLoader.Builder(context).components { add(GifDecoder.Factory()) }.build() }

    fun persistReadPermission(uri: android.net.Uri) { runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { persistReadPermission(uri); profilePhotoUri = uri.toString(); preferences.saveProfilePhotoUri(profilePhotoUri) } }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { persistReadPermission(uri); bannerUri = uri.toString(); preferences.saveProfileBannerUri(bannerUri) } }
    val premiumBannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { persistReadPermission(uri); premiumBannerUri = uri.toString(); preferences.savePremiumBannerUri(premiumBannerUri) } }
    val animatedPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { persistReadPermission(uri); animatedProfileUri = uri.toString(); preferences.saveAnimatedProfileUri(animatedProfileUri) } }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali") }
            Column(Modifier.weight(1f)) { Text("Edit Profile", fontSize = 21.sp, fontWeight = FontWeight.Bold); Text("Customize your profile", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Button(onClick = { preferences.saveProfileName(name); preferences.saveProfileBio(bio); preferences.saveProfileAvatarIndex(avatarIndex); onBack() }, enabled = name.isNotBlank(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(5.dp)); Text("Save") }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp))) {
                if (bannerUri != null) AsyncImage(model = bannerUri, contentDescription = "Banner Atas", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Box(Modifier.fillMaxSize().background(Brush.linearGradient(profileBannerGradients[avatarIndex.coerceIn(0, 3)])))
                Text("KakaAnime", Modifier.align(Alignment.Center).padding(top = 8.dp), fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("ANIME ALWAYS WITH YOU", Modifier.align(Alignment.Center).padding(top = 55.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .72f))
                Surface(Modifier.align(Alignment.BottomStart).padding(14.dp), CircleShape, color = profileAvatarColors[avatarIndex.coerceIn(0, 3)]) {
                    Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
                        when {
                            isPremium && animatedProfileUri != null -> AsyncImage(model = animatedProfileUri, imageLoader = gifImageLoader, contentDescription = "Animated profile", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            profilePhotoUri != null -> AsyncImage(model = profilePhotoUri, contentDescription = "Foto profil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            else -> Text(name.trim().take(2).ifBlank { "KA" }.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
                Surface(Modifier.align(Alignment.BottomStart).padding(start = 76.dp, bottom = 10.dp), CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .92f)) { Icon(Icons.Outlined.Edit, "Edit avatar", Modifier.padding(9.dp).size(17.dp)) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Profile Information", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = name, onValueChange = { name = it.take(20) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Username") }, supportingText = { Text("${name.length}/20") }, shape = RoundedCornerShape(16.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it.take(100) }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 4, label = { Text("Bio") }, supportingText = { Text("${bio.length}/100") }, shape = RoundedCornerShape(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Foto Profil", style = MaterialTheme.typography.titleMedium)
                MediaButton("Ubah foto profil", "JPG, PNG, atau WEBP", Icons.Outlined.Photo) { photoPicker.launch(arrayOf("image/jpeg", "image/png", "image/webp")) }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) { profileAvatarColors.forEachIndexed { index, color -> Surface(Modifier.size(66.dp).clickable { avatarIndex = index }, CircleShape, color = color.copy(alpha = if (avatarIndex == index) 1f else .62f)) { Box(contentAlignment = Alignment.Center) { Text(name.trim().take(2).ifBlank { "KA" }.uppercase(), fontWeight = FontWeight.Black, color = Color.White) } } } }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Premium Media", style = MaterialTheme.typography.titleMedium)
                MediaButton("Banner Atas", if (bannerUri != null) "Custom banner tersimpan" else "Upload banner profil", Icons.Outlined.Photo, locked = !isPremium) { if (isPremium) bannerPicker.launch(arrayOf("image/*")) else onPremiumClick() }
                MediaButton("Banner Premium", if (premiumBannerUri != null) "Custom banner tersimpan" else "Upload banner premium", Icons.Outlined.Photo, locked = !isPremium) { if (isPremium) premiumBannerPicker.launch(arrayOf("image/*")) else onPremiumClick() }
                MediaButton("Animated Profile", if (animatedProfileUri != null) "Animasi tersimpan" else "GIF / animasi profil", Icons.Outlined.Photo, locked = !isPremium) { if (isPremium) animatedPicker.launch(arrayOf("image/gif", "image/*")) else onPremiumClick() }
                if (!isPremium) Text("Fitur Premium terkunci. Ketuk untuk Upgrade to Premium.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MediaButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, locked: Boolean = false, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (locked) Icons.Outlined.Lock else icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("›", fontSize = 23.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
