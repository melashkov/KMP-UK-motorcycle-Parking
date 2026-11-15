package com.melashkov.mcparking.ui.settings

import androidx.lifecycle.ViewModel
import com.melashkov.mcparking.di.DebugApiPreferences
import com.melashkov.mcparking.ui.theme.ThemeMode
import com.melashkov.mcparking.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val debugApiPreferences: DebugApiPreferences,
) : ViewModel() {
    val themeMode = themePreferences.mode

    private val _localBackend = MutableStateFlow(debugApiPreferences.local.takeIf { debugApiPreferences.canSelectLocal })
    val localBackend = _localBackend.asStateFlow()

    fun selectTheme(mode: ThemeMode) {
        themePreferences.setMode(mode)
    }

    fun selectBackend(local: Boolean) {
        val current = _localBackend.value ?: return
        if (current == local || !debugApiPreferences.selectLocal(local)) return
        _localBackend.value = local
    }
}
