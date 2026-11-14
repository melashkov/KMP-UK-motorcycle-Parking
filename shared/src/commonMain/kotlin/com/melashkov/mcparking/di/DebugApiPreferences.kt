package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.PreferenceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

internal class DebugApiPreferences(private val store: PreferenceStore) {
    var localDevelopment: Boolean
        get() = store.getBoolean("local_development")
        set(value) = store.putBoolean("local_development", value)
}

@Serializable
internal data class DebugApiSelection(
    val local: Boolean,
    val revision: Long = 0,
)

internal class DebugApiBackend(
    private val preferences: DebugApiPreferences,
    val enabled: Boolean,
) {
    private val _selection = MutableStateFlow(DebugApiSelection(enabled && preferences.localDevelopment))
    val selection = _selection.asStateFlow()

    fun selectLocal(local: Boolean) {
        val current = _selection.value
        if (!enabled || current.local == local) return
        preferences.localDevelopment = local
        _selection.value = DebugApiSelection(local, current.revision + 1)
    }

    fun activeEnvironment(localEnvironment: ApiEnvironment): ApiEnvironment =
        if (selection.value.local) localEnvironment else ApiEnvironment.Production
}
