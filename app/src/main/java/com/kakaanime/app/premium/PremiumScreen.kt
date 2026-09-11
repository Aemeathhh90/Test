package com.kakaanime.app.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.monetization.MonetizationState

@Composable
fun PremiumScreen(
    state: MonetizationState,
    onSubscribe: () -> Unit
) {
    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("KakaAnime Premium", fontSize = 28.sp, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Nonton tanpa batas dengan fitur premium yang tetap memakai bahasa visual KakaAnime.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = .10f)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                PremiumFeature(Icons.Outlined.HighQuality, "1080p", "Akses kualitas 1080p")
                PremiumFeature(Icons.Outlined.FastForward, "Auto Skip", "Lewati intro dan outro otomatis")
                PremiumFeature(Icons.Outlined.CloudDownload, "Offline", "Download dan tonton tanpa internet")
                Spacer(Modifier.height(2.dp))
                Button(onClick = onSubscribe, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(if (state.isPremium) "Premium Aktif" else "Berlangganan Premium")
                }
            }
        }
    }
}

@Composable
private fun PremiumFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(title, fontSize = 15.sp, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
