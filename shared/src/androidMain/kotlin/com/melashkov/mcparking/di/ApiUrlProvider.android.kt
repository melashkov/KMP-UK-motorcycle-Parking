package com.melashkov.mcparking.di

import android.os.Build
import org.koin.core.scope.Scope

internal actual fun platformSupportsLocalBackend(): Boolean =
    Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish"

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val preferences = scope.get<DebugApiPreferences>()
    return apiUrlProvider(ApiEnvironment.AndroidEmulatorDevelopment) { preferences.local }
}
