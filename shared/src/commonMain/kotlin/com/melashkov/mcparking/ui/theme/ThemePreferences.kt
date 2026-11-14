package com.melashkov.mcparking.ui.theme

import com.melashkov.mcparking.data.preferences.PreferenceStore

internal class ThemePreferences(private val store: PreferenceStore) {
    var mode: ThemeMode
        get() = themeModeFromStorage(store.getString("theme_mode").orEmpty())
        set(value) = store.putString("theme_mode", value.name)
}

internal fun themeModeFromStorage(value: String): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == value } ?: ThemeMode.SYSTEM
