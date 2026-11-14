package com.melashkov.mcparking.di

import android.content.Context
import android.util.AtomicFile
import org.koin.core.scope.Scope
import java.io.File
import java.util.UUID

internal actual fun platformInstallationIdProvider(scope: Scope): InstallationIdProvider {
    val context = scope.get<Context>().applicationContext
    return InstallationIdProvider { ApiInstallationId.get(context) }
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
