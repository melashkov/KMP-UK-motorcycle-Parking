package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.AtomicFile
import org.koin.core.scope.Scope
import java.io.File
import java.util.UUID

internal actual fun platformApiClientMetadata(scope: Scope): ApiClientMetadata {
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
        installationId = ApiInstallationId.get(context),
    )
}

private object ApiInstallationId {
    @Synchronized
    fun get(context: Context): String {
        // Excluded from backup and device transfer; updates keep the ID, reinstalls reset it.
        val file = AtomicFile(File(context.noBackupFilesDir, "api-installation-id"))
        val existing = runCatching { UUID.fromString(file.readFully().toString(Charsets.UTF_8)).toString() }.getOrNull()
        if (existing != null) return existing
        val id = UUID.randomUUID().toString()
        val output = file.startWrite()
        try {
            output.write(id.toByteArray(Charsets.UTF_8))
            file.finishWrite(output)
        } catch (error: Exception) {
            file.failWrite(output)
            throw error
        }
        return id
    }
}
