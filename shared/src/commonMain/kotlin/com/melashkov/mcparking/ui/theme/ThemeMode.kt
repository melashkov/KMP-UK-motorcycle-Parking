package com.melashkov.mcparking.ui.theme

enum class ThemeMode { SYSTEM, LIGHT, DARK }

internal fun ThemeMode.isDark(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
