package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.PreferenceStore
import kotlin.concurrent.Volatile

internal class DebugApiPreferences(
    private val store: PreferenceStore,
    val canSelectLocal: Boolean,
) {

    @Volatile
    var local: Boolean = false
        private set

    init {
        if (canSelectLocal) {
            local = store.getBoolean("local_development")
        }
    }

    fun selectLocal(value: Boolean): Boolean {
        if (canSelectLocal) {
            runCatching {
                store.putBoolean("local_development", value)
                local = value
                return true
            }
        }
        return false
    }
}
