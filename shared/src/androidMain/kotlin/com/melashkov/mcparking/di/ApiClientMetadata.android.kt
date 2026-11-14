package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.koin.core.scope.Scope

internal actual fun platformApiClientMetadata(scope: Scope, installationId: String): ApiClientMetadata {
    val context = scope.get<Context>().applicationContext
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        info.versionCode.toLong()
    }
    return ApiClientMetadata(
        platform = "android",
        appVersion = info.versionName.orEmpty(),
        appVersionCode = versionCode.toString(),
        osVersion = Build.VERSION.RELEASE,
        androidApiLevel = Build.VERSION.SDK_INT,
        installationId = installationId,
    )
}
