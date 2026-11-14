package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.scope.Scope

internal actual fun platformDebugApiPreferences(scope: Scope): DebugApiPreferences {
    val context = scope.get<Context>()
    val preferences = context.getSharedPreferences("debug_api", Context.MODE_PRIVATE)
    return object : DebugApiPreferences {
        override val enabled = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        override var localDevelopment: Boolean
            get() = enabled && preferences.getBoolean("local_development", false)
            set(value) {
                if (enabled) preferences.edit().putBoolean("local_development", value).apply()
            }
    }
}
