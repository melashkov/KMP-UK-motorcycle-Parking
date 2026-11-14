package com.melashkov.mcparking.data.preferences

import org.koin.core.scope.Scope

internal interface PreferenceStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String): Boolean
    fun putBoolean(key: String, value: Boolean)
}

internal enum class PreferenceNamespace(val fileName: String) {
    Appearance("appearance"),
    DeveloperMessage("developer_message"),
    DebugApi("debug_api"),
}

internal expect fun platformPreferenceStore(scope: Scope, namespace: PreferenceNamespace): PreferenceStore
