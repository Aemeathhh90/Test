package com.kakaanime.app.monetization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EpisodeGateDialog(
    episodeNumber: Int,
    episodeTitle: String? = null,
    episodeThumbnailUrl: String? = null,
    episodeReleasedAt: Long? = null,
    state: MonetizationState,
    onDismiss: () -> Unit,
    onWatchAdAndUnlock: () -> Unit,
    onWaitForUnlock: () -> Unit,
    onStartPremium: () -> Unit,
) {
    val dateText = remember(episodeReleasedAt) {
        episodeReleasedAt?.takeIf { it > 0L }?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        }
    }

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
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.38f)),
                        )
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Episode $episodeNumber", fontWeight = FontWeight.SemiBold)
                        if (!episodeTitle.isNullOrBlank()) {
                            Text(episodeTitle, fontWeight = FontWeight.Bold, maxLines = 2)
                        }
                        if (dateText != null) {
                            Text(dateText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text("Episode Terkunci", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Pilih cara membuka episode. Countdown akan dilanjutkan di Player, jadi kamu tidak perlu menunggu di dialog ini.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onWatchAdAndUnlock,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Tonton Iklan & Buka")
                }

                OutlinedButton(
                    onClick = onWaitForUnlock,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Tunggu 90 Detik")
                }

                OutlinedButton(onClick = onStartPremium, modifier = Modifier.fillMaxWidth()) {
                    Text("Premium • Tanpa Menunggu")
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Reward iklan atau countdown memberi 2 diamond. 1 diamond langsung digunakan untuk episode ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                if (state.diamonds > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("Diamond tersedia: ${state.diamonds}", fontSize = 13.sp)
                }
            }
        },
    )
}
