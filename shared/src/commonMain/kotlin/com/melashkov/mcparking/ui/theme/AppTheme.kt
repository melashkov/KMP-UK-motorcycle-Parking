package com.melashkov.mcparking.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import org.koin.compose.koinInject
import org.maplibre.compose.style.BaseStyle

internal data class ThemeSettings(val mode: ThemeMode, val setMode: (ThemeMode) -> Unit)
internal val LocalThemeSettings = staticCompositionLocalOf { ThemeSettings(ThemeMode.SYSTEM) {} }
internal val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
internal fun ParkingAppTheme(content: @Composable () -> Unit) {
    val preferences = koinInject<ThemePreferences>()
    var mode by remember(preferences) { mutableStateOf(preferences.mode) }
    val dark = mode.isDark(isSystemInDarkTheme())
    ApplyPlatformTheme(dark, mode == ThemeMode.SYSTEM)
    CompositionLocalProvider(
        LocalDarkTheme provides dark,
        LocalThemeSettings provides ThemeSettings(mode) {
            preferences.mode = it
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
