package com.melashkov.mcparking.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberThemePreferences(): ThemePreferences {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        object : ThemePreferences {
            override var mode: String
                get() = preferences.getString("theme_mode", "").orEmpty()
                set(value) { preferences.edit().putString("theme_mode", value).apply() }
        }
    }
}
