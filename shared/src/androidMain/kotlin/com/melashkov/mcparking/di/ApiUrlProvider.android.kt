package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val applicationInfo = scope.get<Context>().applicationInfo
    val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    val environment =
        when {
            !isDebug -> ApiEnvironment.Production
            isAndroidEmulator() -> ApiEnvironment.AndroidEmulatorDevelopment
            else -> ApiEnvironment.LocalhostDevelopment
        }
    return apiUrlProvider(environment)
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
