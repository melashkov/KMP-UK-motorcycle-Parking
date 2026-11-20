package com.melashkov.mcparking.ui.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
internal actual fun rememberDeveloperMessagePreferences(): DeveloperMessagePreferences = remember {
    object : DeveloperMessagePreferences {
        override var lastSeenRelease: String
            get() = NSUserDefaults.standardUserDefaults.stringForKey("last_seen_release").orEmpty()
            set(value) {
                NSUserDefaults.standardUserDefaults.setObject(value, forKey = "last_seen_release")
            }
    }
}
