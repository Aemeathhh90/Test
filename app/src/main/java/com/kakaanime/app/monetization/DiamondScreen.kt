package com.kakaanime.app.monetization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiamondScreen(
    state: MonetizationState,
    onBack: () -> Unit,
    onWatchAd: () -> Unit,
    onTopUp: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Kembali") }
            Text("Diamond", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }

        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Saldo Diamond", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("💎 ${state.diamonds}", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("1 Diamond = 1 episode", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text("Cara mendapatkan Diamond", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tonton iklan reward")
                Text("Dapatkan +2 Diamond setiap iklan yang berhasil diselesaikan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onWatchAd, modifier = Modifier.fillMaxWidth()) { Text("Tonton Iklan +2 💎") }
            }
        }

        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Top Up Diamond")
                Text("Pembelian paket Diamond akan dihubungkan ke billing resmi pada tahap monetisasi berikutnya.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onTopUp, modifier = Modifier.fillMaxWidth()) { Text("Lihat Paket") }
            }
        }
    }
}
