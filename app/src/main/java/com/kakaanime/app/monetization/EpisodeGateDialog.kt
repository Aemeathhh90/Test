package com.kakaanime.app.monetization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * V1 Episode Gate foundation.
 *
 * This dialog is only shown when a free user tries to open a locked episode
 * without a diamond. There is no standalone "watch ad for diamonds" action.
 * The rewarded ad grants 2 diamonds and one diamond is consumed immediately
 * for the requested episode. After the reward, the dialog closes and playback
 * opens directly; there is no success popup.
 */
@Composable
fun EpisodeGateDialog(
    episodeNumber: Int,
    episodeTitle: String? = null,
    state: MonetizationState,
    onDismiss: () -> Unit,
    onWatchAdAndUnlock: () -> Unit,
    onStartPremium: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Episode Terkunci", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    buildString {
                        append("Episode $episodeNumber")
                        if (!episodeTitle.isNullOrBlank()) append(" • $episodeTitle")
                    },
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Kamu memerlukan akses untuk menonton episode ini.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Premium", fontWeight = FontWeight.Bold)
                    Text("Akses episode tanpa rewarded ad dan hingga 1080p.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onStartPremium, modifier = Modifier.fillMaxWidth()) {
                    Text("Mulai Premium")
                }
                Text("atau", modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = onWatchAdAndUnlock, modifier = Modifier.fillMaxWidth()) {
                    Text("Tonton Iklan & Buka")
                }
                Text(
                    "Rewarded ad memberi 2 diamond; 1 diamond langsung digunakan untuk episode ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.diamonds > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text("Diamond tersedia: ${state.diamonds}")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}
