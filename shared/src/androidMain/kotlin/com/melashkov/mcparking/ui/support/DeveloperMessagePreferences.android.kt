package com.melashkov.mcparking.ui.support

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberDeveloperMessagePreferences(): DeveloperMessagePreferences {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("developer_message", Context.MODE_PRIVATE)
        object : DeveloperMessagePreferences {
            override var lastSeenRelease: String
                get() = preferences.getString("last_seen_release", "").orEmpty()
                set(value) { preferences.edit().putString("last_seen_release", value).apply() }
        }
    }
}
