package com.melashkov.mcparking.di

import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberApiUrlProvider(): ApiUrlProvider {
    val applicationInfo = LocalContext.current.applicationInfo
    val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    val environment =
        when {
            !isDebug -> ApiEnvironment.Production
            isAndroidEmulator() -> ApiEnvironment.AndroidEmulatorDevelopment
            else -> ApiEnvironment.LocalhostDevelopment
        }
    return remember(environment) { apiUrlProvider(environment) }
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
