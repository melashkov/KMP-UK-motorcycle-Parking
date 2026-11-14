package com.melashkov.mcparking.data.preferences

import org.koin.core.scope.Scope
import platform.Foundation.NSUserDefaults

internal actual fun platformPreferenceStore(scope: Scope, namespace: PreferenceNamespace): PreferenceStore =
    UserDefaultsPreferenceStore(NSUserDefaults.standardUserDefaults, namespace)

private class UserDefaultsPreferenceStore(
    private val preferences: NSUserDefaults,
    private val namespace: PreferenceNamespace,
) : PreferenceStore {
    // Preserve the two legacy flat keys. The existing debug API key already has a prefix.
    private val legacyUnprefixedKey = when (namespace) {
        PreferenceNamespace.Appearance -> "theme_mode"
        PreferenceNamespace.DeveloperMessage -> "last_seen_release"
        PreferenceNamespace.DebugApi -> null
    }

    private fun storedKey(key: String): String =
        if (key == legacyUnprefixedKey) key else "${namespace.fileName}_$key"

    override fun getString(key: String): String? = preferences.stringForKey(storedKey(key))

    override fun putString(key: String, value: String) = preferences.setObject(value, forKey = storedKey(key))

    override fun getBoolean(key: String): Boolean = preferences.boolForKey(storedKey(key))

    override fun putBoolean(key: String, value: Boolean) = preferences.setBool(value, forKey = storedKey(key))
}
