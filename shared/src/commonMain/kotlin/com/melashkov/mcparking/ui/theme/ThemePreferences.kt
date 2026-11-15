package com.melashkov.mcparking.ui.theme

import com.melashkov.mcparking.data.preferences.PreferenceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ThemePreferences(private val store: PreferenceStore) {
    private val _mode = MutableStateFlow(themeModeFromStorage(store.getString("theme_mode").orEmpty()))
    val mode = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        if (_mode.value == mode) return
        store.putString("theme_mode", mode.name)
        _mode.value = mode
    }
}

internal fun themeModeFromStorage(value: String): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == value } ?: ThemeMode.SYSTEM
