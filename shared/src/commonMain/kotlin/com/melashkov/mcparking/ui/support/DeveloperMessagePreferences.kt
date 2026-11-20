package com.melashkov.mcparking.ui.support

import androidx.compose.runtime.Composable

internal interface DeveloperMessagePreferences {
    var lastSeenRelease: String
}

@Composable
internal expect fun rememberDeveloperMessagePreferences(): DeveloperMessagePreferences
