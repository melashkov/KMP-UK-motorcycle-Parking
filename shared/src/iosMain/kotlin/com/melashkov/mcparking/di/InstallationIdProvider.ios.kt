package com.melashkov.mcparking.di

import org.koin.core.scope.Scope
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

internal actual fun platformInstallationIdProvider(scope: Scope): InstallationIdProvider = IosInstallationIdProvider

private object IosInstallationIdProvider : InstallationIdProvider {
    private val id: String by lazy {
        val preferences = NSUserDefaults.standardUserDefaults
        preferences.stringForKey("api_installation_id") ?: NSUUID().UUIDString.lowercase().also {
            preferences.setObject(it, forKey = "api_installation_id")
        }
    }

    override fun getOrCreate(): String = id
}
