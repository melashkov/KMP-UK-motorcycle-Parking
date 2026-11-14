package com.melashkov.mcparking.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.koin.core.scope.Scope

internal actual fun platformPreferenceStore(scope: Scope, namespace: PreferenceNamespace): PreferenceStore {
    val context = scope.get<Context>().applicationContext
    return SharedPreferencesStore(context.getSharedPreferences(namespace.fileName, Context.MODE_PRIVATE))
}

private class SharedPreferencesStore(private val preferences: SharedPreferences) : PreferenceStore {
    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) = preferences.edit { putString(key, value) }

    override fun getBoolean(key: String): Boolean = preferences.getBoolean(key, false)

    override fun putBoolean(key: String, value: Boolean) = preferences.edit { putBoolean(key, value) }
}
