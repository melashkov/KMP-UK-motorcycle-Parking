package com.melashkov.mcparking.data.preferences

internal class MemoryPreferenceStore : PreferenceStore {
    private val values = mutableMapOf<String, Any>()

    override fun getString(key: String): String? = values[key] as String?

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getBoolean(key: String): Boolean = (values[key] as Boolean?) ?: false

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }
}
