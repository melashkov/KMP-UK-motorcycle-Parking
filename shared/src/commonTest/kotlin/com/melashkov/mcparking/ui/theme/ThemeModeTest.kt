package com.melashkov.mcparking.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {
    @Test
    fun systemThemeTracksDeviceWhileOverridesStayFixed() {
        assertFalse(ThemeMode.SYSTEM.isDark(false))
        assertTrue(ThemeMode.SYSTEM.isDark(true))
        assertFalse(ThemeMode.LIGHT.isDark(true))
        assertTrue(ThemeMode.DARK.isDark(false))
    }

    @Test
    fun savedChoicesRoundTripAndUnknownValuesFollowSystem() {
        ThemeMode.entries.forEach { assertEquals(it, themeModeFromStorage(it.name)) }
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage(""))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage("unknown"))
    }
}
