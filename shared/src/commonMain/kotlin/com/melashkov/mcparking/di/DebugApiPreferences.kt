package com.melashkov.mcparking.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.scope.Scope

internal interface DebugApiPreferences {
    val enabled: Boolean
    var localDevelopment: Boolean
}

internal data class DebugApiSelection(
    val activeLocal: Boolean,
    val nextLaunchLocal: Boolean = activeLocal,
) {
    val restartRequired: Boolean get() = activeLocal != nextLaunchLocal
}

internal class DebugApiBackend(private val preferences: DebugApiPreferences) {
    val enabled = preferences.enabled
    private val _selection = MutableStateFlow(DebugApiSelection(enabled && preferences.localDevelopment))
    val selection = _selection.asStateFlow()

    fun selectLocal(local: Boolean) {
        if (!enabled) return
        preferences.localDevelopment = local
        _selection.value = _selection.value.copy(nextLaunchLocal = local)
    }

    fun activeEnvironment(localEnvironment: ApiEnvironment): ApiEnvironment =
        if (selection.value.activeLocal) localEnvironment else ApiEnvironment.Production
}

internal expect fun platformDebugApiPreferences(scope: Scope): DebugApiPreferences
