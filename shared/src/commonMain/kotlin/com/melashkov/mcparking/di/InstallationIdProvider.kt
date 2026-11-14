package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal fun interface InstallationIdProvider {
    fun getOrCreate(): String
}

internal expect fun platformInstallationIdProvider(scope: Scope): InstallationIdProvider
