package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal actual fun platformSupportsLocalBackend(): Boolean = true

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val preferences = scope.get<DebugApiPreferences>()
    return apiUrlProvider(ApiEnvironment.LocalhostDevelopment) { preferences.local }
}
