package com.melashkov.mcparking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import org.maplibre.compose.style.BaseStyle

enum class ThemeMode { SYSTEM, LIGHT, DARK }

internal interface ThemePreferences {
    var mode: String
}

@Composable
internal expect fun rememberThemePreferences(): ThemePreferences

internal data class ThemeSettings(val mode: ThemeMode, val setMode: (ThemeMode) -> Unit)
internal val LocalThemeSettings = staticCompositionLocalOf { ThemeSettings(ThemeMode.SYSTEM) {} }
internal val LocalDarkTheme = staticCompositionLocalOf { false }

internal fun themeModeFromStorage(value: String): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == value } ?: ThemeMode.SYSTEM

internal fun ThemeMode.isDark(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
internal fun ParkingAppTheme(content: @Composable () -> Unit) {
    val preferences = rememberThemePreferences()
    var mode by remember { mutableStateOf(themeModeFromStorage(preferences.mode)) }
    val dark = mode.isDark(isSystemInDarkTheme())
    ApplyPlatformTheme(dark, mode == ThemeMode.SYSTEM)
    CompositionLocalProvider(
        LocalDarkTheme provides dark,
        LocalThemeSettings provides ThemeSettings(mode) {
            preferences.mode = it.name
            mode = it
        },
    ) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(), content = content)
    }
}

@Composable
internal fun parkingMapStyle(): BaseStyle =
    if (LocalDarkTheme.current) DarkMapStyle else LightMapStyle

private val LightMapStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
private val DarkMapStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/fiord")

@Composable
internal expect fun ApplyPlatformTheme(dark: Boolean, followSystem: Boolean)
