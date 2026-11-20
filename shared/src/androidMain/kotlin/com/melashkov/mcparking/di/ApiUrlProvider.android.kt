package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val context = scope.get<Context>()
    val environment =
        when (context.apiEnvironment()) {
            DEVELOPMENT_API_ENVIRONMENT -> ApiEnvironment.AndroidEmulatorDevelopment
            else -> ApiEnvironment.Production
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

private const val API_ENVIRONMENT_META_DATA = "com.melashkov.mcparking.API_ENVIRONMENT"
private const val DEVELOPMENT_API_ENVIRONMENT = "development"
