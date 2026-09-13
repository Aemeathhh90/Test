package com.kakaanime.app.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.ui.theme.KakaTokens

@Composable
fun PremiumScreen(
    state: MonetizationState,
    billingState: PremiumBillingState,
    onBack: () -> Unit,
    onSubscribe: (String) -> Unit,
    onRetryBilling: () -> Unit
) {
    val scrollState = rememberScrollState()
    val availableOffers = (billingState as? PremiumBillingState.Ready)?.offers.orEmpty()
    var selectedPlanId by remember(availableOffers) {
        mutableStateOf(availableOffers.firstOrNull()?.plan?.basePlanId ?: "monthly")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = KakaTokens.screenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(KakaTokens.sectionGap)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ArrowBack,
                contentDescription = "Kembali",
                modifier = Modifier
                    .clip(RoundedCornerShape(KakaTokens.smallRadius))
                    .clickable { onBack() }
                    .padding(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("Premium", style = MaterialTheme.typography.titleLarge)
        }

        PremiumHero(isPremium = state.isPremium)

        Text("Pilih Paket Premium", style = MaterialTheme.typography.titleLarge)
        Text(
            "Nikmati pengalaman nonton tanpa batas dengan pembayaran melalui Google Play.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        if (billingState is PremiumBillingState.Loading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(KakaTokens.cardRadius)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.width(22.dp).height(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Mengambil paket dari Google Play…")
                }
            }
        }

        if (availableOffers.isNotEmpty()) {
            availableOffers.forEach { offer ->
                PremiumPlanCard(
                    offer = offer,
                    selected = selectedPlanId == offer.plan.basePlanId,
                    onClick = { selectedPlanId = offer.plan.basePlanId }
                )
            }
        } else if (billingState is PremiumBillingState.Unavailable) {
            BillingUnavailableCard(billingState.message, onRetryBilling)
        }

        Text("Keuntungan Premium", style = MaterialTheme.typography.titleLarge)
        PremiumBenefitCard()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KakaTokens.cardRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Pembayaran Google Play", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Harga dan metode pembayaran mengikuti akun Google Play kamu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Button(
            onClick = { onSubscribe(selectedPlanId) },
            enabled = !state.isPremium && availableOffers.any { it.plan.basePlanId == selectedPlanId },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(KakaTokens.pillRadius)
        ) {
            Icon(Icons.Outlined.WorkspacePremium, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.isPremium) "Premium Aktif" else "Lanjut ke Google Play")
        }

        Text(
            "Dengan berlangganan, kamu menyetujui ketentuan langganan yang berlaku di Google Play.",
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun PremiumHero(isPremium: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .72f),
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(KakaTokens.pillRadius),
                color = MaterialTheme.colorScheme.background.copy(alpha = .72f)
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPremium) "Premium User" else "KakaAnime Premium", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("More Anime,\nMore Memories.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                if (isPremium) "Nikmati semua fitur Premium kamu."
                else "Nonton lebih bebas tanpa batas Diamond.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PremiumPlanCard(
    offer: PremiumOffer,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(KakaTokens.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = .12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(offer.plan.title, style = MaterialTheme.typography.titleMedium)
                    offer.plan.savingLabel?.let {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(KakaTokens.pillRadius),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .16f)
                        ) {
                            Text(it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(offer.formattedPrice, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PremiumBenefitCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KakaTokens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PremiumBenefit(Icons.Outlined.WorkspacePremium, "Nonton Tanpa Batas", "Akses episode tanpa perlu Diamond.")
            PremiumBenefit(Icons.Outlined.HighQuality, "1080p", "Nikmati kualitas streaming Full HD.")
            PremiumBenefit(Icons.Outlined.FastForward, "Skip Intro & Outro", "Langsung ke bagian cerita tanpa menunggu.")
            PremiumBenefit(Icons.Outlined.CloudDownload, "Download Episode", "Simpan episode untuk ditonton offline.")
        }
    }
}

@Composable
private fun PremiumBenefit(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.width(42.dp).height(42.dp),
            shape = RoundedCornerShape(13.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BillingUnavailableCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KakaTokens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Paket belum tersedia", style = MaterialTheme.typography.titleMedium)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(KakaTokens.pillRadius)) {
                Text("Coba lagi")
            }
        }
    }
}
