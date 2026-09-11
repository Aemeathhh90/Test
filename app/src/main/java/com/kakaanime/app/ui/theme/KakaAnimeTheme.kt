package com.kakaanime.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class KakaAccent(
    val primary: Color,
    val secondary: Color
) {
    Blue(
        Color(0xFF6C63FF),
        Color(0xFF8B83FF)
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
fun rememberKakaThemeState(): KakaThemeState {
    return remember {
        KakaThemeState()
    }
}

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
            background = Color(0xFF0B0B0F),
            surface = Color(0xFF141419),
            surfaceVariant = Color(0xFF1C1C23)
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            secondary = accent.secondary,
            background = Color(0xFFF7F7FA),
            surface = Color.White,
            surfaceVariant = Color(0xFFEDEDF3)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
