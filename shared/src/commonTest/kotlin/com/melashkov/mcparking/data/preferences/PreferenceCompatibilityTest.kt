package com.melashkov.mcparking.data.preferences

import com.melashkov.mcparking.ui.support.DeveloperMessagePreferences
import com.melashkov.mcparking.ui.theme.ThemeMode
import com.melashkov.mcparking.ui.theme.ThemePreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferenceCompatibilityTest {
    @Test
    fun themeReadsAndWritesTheExistingModeKey() {
        val store = MemoryPreferenceStore()
        assertEquals(ThemeMode.SYSTEM, ThemePreferences(store).mode.value)
        store.putString("theme_mode", "DARK")
        val preferences = ThemePreferences(store)
        assertEquals(ThemeMode.DARK, preferences.mode.value)

        preferences.setMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, preferences.mode.value)
        assertEquals("LIGHT", store.getString("theme_mode"))
    }

    @Test
    fun developerMessageReadsAndWritesTheExistingReleaseKey() {
        val store = MemoryPreferenceStore()
        val preferences = DeveloperMessagePreferences(store)
        assertEquals("", preferences.lastSeenRelease)
        store.putString("last_seen_release", "previous-release")
        assertEquals("previous-release", preferences.lastSeenRelease)

        preferences.lastSeenRelease = "current-release"
        assertEquals("current-release", store.getString("last_seen_release"))
    }
}
