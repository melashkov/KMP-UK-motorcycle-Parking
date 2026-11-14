package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.PreferenceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DebugApiPreferences(private val store: PreferenceStore) {
    var localDevelopment: Boolean
        get() = store.getBoolean("local_development")
        set(value) = store.putBoolean("local_development", value)
}

internal data class DebugApiSelection(
    val activeLocal: Boolean,
    val nextLaunchLocal: Boolean = activeLocal,
) {
    val restartRequired: Boolean get() = activeLocal != nextLaunchLocal
}

internal class DebugApiBackend(
    private val preferences: DebugApiPreferences,
    val enabled: Boolean,
) {
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
