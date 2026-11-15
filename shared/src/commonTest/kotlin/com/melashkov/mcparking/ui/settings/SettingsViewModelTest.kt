package com.melashkov.mcparking.ui.settings

import com.melashkov.mcparking.data.preferences.MemoryPreferenceStoreStub
import com.melashkov.mcparking.data.preferences.PreferenceStore
import com.melashkov.mcparking.di.DebugApiPreferences
import com.melashkov.mcparking.ui.theme.ThemeMode
import com.melashkov.mcparking.ui.theme.ThemePreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest {
    @Test
    fun selectingThemeUpdatesTheSharedThemePreference() {
        val store = MemoryPreferenceStoreStub()
        val theme = ThemePreferences(store)
        val viewModel = SettingsViewModel(theme, DebugApiPreferences(store, isDebug = false))

        viewModel.selectTheme(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
        assertEquals(ThemeMode.DARK, theme.mode.value)
        assertEquals("DARK", store.getString("theme_mode"))
    }

    @Test
    fun backendSelectionUpdatesOnlyAfterASuccessfulWrite() {
        val saved = MemoryPreferenceStoreStub()
        var failWrites = false
        var writes = 0
        val store = object : PreferenceStore by saved {
            override fun putBoolean(key: String, value: Boolean) {
                writes++
                check(!failWrites)
                saved.putBoolean(key, value)
            }
        }
        val viewModel = SettingsViewModel(ThemePreferences(store), DebugApiPreferences(store, isDebug = true))

        viewModel.selectBackend(true)
        viewModel.selectBackend(true)
        assertEquals(1, writes)
        failWrites = true
        viewModel.selectBackend(false)

        assertEquals(true, viewModel.localBackend.value)
        assertTrue(saved.getBoolean("local_development"))
    }

    @Test
    fun releaseSettingsDoNotOfferBackendSelection() {
        val store = MemoryPreferenceStoreStub()
        val viewModel = SettingsViewModel(ThemePreferences(store), DebugApiPreferences(store, isDebug = false))

        viewModel.selectBackend(true)

        assertNull(viewModel.localBackend.value)
    }
}
