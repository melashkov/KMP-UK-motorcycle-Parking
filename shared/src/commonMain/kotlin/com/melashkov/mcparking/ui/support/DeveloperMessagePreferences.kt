package com.melashkov.mcparking.ui.support

import com.melashkov.mcparking.data.preferences.PreferenceStore

internal class DeveloperMessagePreferences(private val store: PreferenceStore) {
    var lastSeenRelease: String
        get() = store.getString("last_seen_release").orEmpty()
        set(value) = store.putString("last_seen_release", value)
}
