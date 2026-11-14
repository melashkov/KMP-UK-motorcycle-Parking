package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val backend = scope.get<DebugApiBackend>()
    return apiUrlProvider { backend.activeEnvironment(ApiEnvironment.LocalhostDevelopment) }
}
