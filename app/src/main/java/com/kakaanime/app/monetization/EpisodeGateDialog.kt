package com.kakaanime.app.monetization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val WAIT_SECONDS = 90

@Composable
fun EpisodeGateDialog(
    episodeNumber: Int,
    episodeTitle: String? = null,
    episodeThumbnailUrl: String? = null,
    episodeReleasedAt: Long? = null,
    state: MonetizationState,
    onDismiss: () -> Unit,
    onWatchAdAndUnlock: () -> Unit,
    onFallbackTimeout: () -> Unit,
    onStartPremium: () -> Unit,
) {
    var waiting by remember { mutableStateOf(false) }
    var remaining by remember { mutableIntStateOf(WAIT_SECONDS) }

    LaunchedEffect(waiting) {
        if (!waiting) return@LaunchedEffect
        remaining = WAIT_SECONDS
        while (remaining > 0) {
            delay(1000L)
            remaining--
        }
        onFallbackTimeout()
    }

    val dateText = remember(episodeReleasedAt) {
        episodeReleasedAt?.takeIf { it > 0L }?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        }
    }
    val progress = remaining.toFloat() / WAIT_SECONDS.toFloat()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("KakaAnime", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp, 62.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = episodeThumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.30f)),
                        )
                        Icon(
                            if (waiting) Icons.Default.PlayArrow else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Episode $episodeNumber", fontWeight = FontWeight.SemiBold)
                        if (!episodeTitle.isNullOrBlank()) Text(episodeTitle, fontWeight = FontWeight.Bold, maxLines = 2)
                        if (dateText != null) Text(dateText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(22.dp))

                if (!waiting) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Episode Terkunci", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tonton iklan untuk membuka episode lebih cepat, atau tunggu waktu akses selesai.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { waiting = true; onWatchAdAndUnlock() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Tonton Iklan & Buka")
                    }
                    OutlinedButton(onClick = onStartPremium, modifier = Modifier.fillMaxWidth()) {
                        Text("Premium • Tanpa Menunggu")
                    }
                    Text(
                        "Rewarded ad memberi 2 diamond; 1 diamond digunakan untuk episode ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    if (state.diamonds > 0) Text("Diamond tersedia: ${state.diamonds}", fontSize = 13.sp)
                } else {
                    Text("Mohon Tunggu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Episode akan terbuka setelah waktu tunggu selesai.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(190.dp)) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(170.dp),
                            strokeWidth = 10.dp,
                        )
                        Text(
                            String.format(Locale.getDefault(), "%02d:%02d", remaining / 60, remaining % 60),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Iklan sedang diputar atau belum tersedia. Jika reward iklan diterima lebih dulu, episode akan langsung terbuka.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.size(10.dp))
                        Text(
                            "Jangan tutup aplikasi selama proses berlangsung. Setelah waktu tunggu selesai, akses episode diproses otomatis.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
                }
            }
        },
    )
}
