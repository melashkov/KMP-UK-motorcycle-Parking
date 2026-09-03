package com.melashkov.mcparking.di

import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private const val EMULATOR_API_BASE_URL = "http://10.0.2.2:8080/api_dev/"
private const val USB_DEVICE_API_BASE_URL = "http://127.0.0.1:8080/api_dev/"

@Composable
internal actual fun rememberApiBaseUrl(): String {
    val applicationInfo = LocalContext.current.applicationInfo
    val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    val developmentApiBaseUrl =
        if (isAndroidEmulator()) EMULATOR_API_BASE_URL else USB_DEVICE_API_BASE_URL
    return apiBaseUrl(isDebug, developmentApiBaseUrl)
}

private fun isAndroidEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.contains("emulator") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk")
