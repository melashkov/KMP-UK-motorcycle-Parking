package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val context = scope.get<Context>()
    val applicationInfo = context.applicationInfo
    val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    val isDevelopmentFlavor = context.apiEnvironment() == DEVELOPMENT_API_ENVIRONMENT
    val environment =
        when {
            !isDebuggable || !isDevelopmentFlavor -> ApiEnvironment.Production
            isAndroidEmulator() -> ApiEnvironment.AndroidEmulatorDevelopment
            else -> ApiEnvironment.LocalhostDevelopment
        }
    return apiUrlProvider(environment)
}

private fun Context.apiEnvironment(): String? {
    val applicationInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }
    return applicationInfo.metaData?.getString(API_ENVIRONMENT_META_DATA)
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

private const val API_ENVIRONMENT_META_DATA = "com.melashkov.mcparking.API_ENVIRONMENT"
private const val DEVELOPMENT_API_ENVIRONMENT = "development"
