package com.kakaanime.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class KakaAccent(
    val primary: Color,
    val secondary: Color
) {
    Blue(
        Color(0xFF2196F3),
        Color(0xFF64B5F6)
    ),
    Purple(
        Color(0xFF9C6BFF),
        Color(0xFFB58CFF)
    ),
    Pink(
        Color(0xFFFF5FA2),
        Color(0xFFFF82B7)
    ),
    Red(
        Color(0xFFFF4D67),
        Color(0xFFFF7186)
    ),
    Green(
        Color(0xFF35C98A),
        Color(0xFF5BE0A6)
    ),
    Cyan(
        Color(0xFF20C8E8),
        Color(0xFF58DDF2)
    ),
    Orange(
        Color(0xFFFF9A3D),
        Color(0xFFFFB66B)
    )
}

@Stable
class KakaThemeState(
    accent: KakaAccent = KakaAccent.Blue,
    darkMode: Boolean = true
) {
    var accent by mutableStateOf(accent)
    var darkMode by mutableStateOf(darkMode)
}

@Composable
fun rememberKakaThemeState(): KakaThemeState = remember { KakaThemeState() }

@Composable
fun KakaAnimeTheme(
    themeState: KakaThemeState = rememberKakaThemeState(),
    content: @Composable () -> Unit
) {
    val accent = themeState.accent
    val colors = if (themeState.darkMode) {
        darkColorScheme(
            primary = accent.primary,
            secondary = accent.secondary,
            background = Color(0xFF090D12),
            surface = Color(0xFF111820),
            surfaceVariant = Color(0xFF17212B)
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            secondary = accent.secondary,
            background = Color(0xFFF5F8FC),
            surface = Color.White,
            surfaceVariant = Color(0xFFE9F0F7)
        )
    }

    MaterialTheme(colorScheme = colors, content = content)
}
