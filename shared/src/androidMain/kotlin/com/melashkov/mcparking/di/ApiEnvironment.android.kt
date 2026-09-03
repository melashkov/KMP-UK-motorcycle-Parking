package com.melashkov.mcparking.di

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberApiBaseUrl(): String {
    val applicationInfo = LocalContext.current.applicationInfo
    val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    return apiBaseUrl(isDebug)
}
