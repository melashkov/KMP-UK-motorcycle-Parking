package com.melashkov.mcparking.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberThemePreferences(): ThemePreferences = remember {
    object : ThemePreferences {
        override var mode: String
            get() = NSUserDefaults.standardUserDefaults.stringForKey("theme_mode").orEmpty()
            set(value) {
                NSUserDefaults.standardUserDefaults.setObject(value, forKey = "theme_mode")
            }
    }
}
