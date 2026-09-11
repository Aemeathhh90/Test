package com.kakaanime.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Star
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
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaThemeState

@Composable
fun ProfileScreen(
    themeState: KakaThemeState,
    monetizationState: MonetizationState,
    onPremiumClick: () -> Unit
) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profile", fontSize = 28.sp, style = MaterialTheme.typography.headlineSmall)
        Text("Atur pengalaman KakaAnime", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Star, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (monetizationState.isPremium) "Premium Aktif" else "Free", fontSize = 16.sp, style = MaterialTheme.typography.titleMedium)
                        Text("${monetizationState.diamonds} diamond", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = onPremiumClick, shape = RoundedCornerShape(14.dp)) {
                        Text("Premium")
                    }
                }
            }
        }

        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DarkMode, null)
                    Spacer(Modifier.padding(6.dp))
                    Text("Dark mode", Modifier.weight(1f))
                    Text(if (themeState.darkMode) "ON" else "OFF", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { themeState.darkMode = !themeState.darkMode })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Palette, null)
                    Spacer(Modifier.padding(6.dp))
                    Text("Accent", Modifier.weight(1f))
                    Text(themeState.accent.name, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    KakaAccent.entries.forEach { accent ->
                        Surface(
                            modifier = Modifier.weight(1f).height(34.dp).clickable { themeState.accent = accent },
                            shape = RoundedCornerShape(10.dp),
                            color = accent.primary.copy(alpha = if (themeState.accent == accent) .9f else .22f)
                        ) {}
                    }
                }
            }
        }
    }
}
