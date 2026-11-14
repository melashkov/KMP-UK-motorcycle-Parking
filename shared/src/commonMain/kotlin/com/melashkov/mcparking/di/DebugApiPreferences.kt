package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal interface DebugApiPreferences {
    val enabled: Boolean
    var localDevelopment: Boolean
}

internal fun debugApiEnvironment(
    preferences: DebugApiPreferences,
    localEnvironment: ApiEnvironment,
): ApiEnvironment =
    if (preferences.enabled && preferences.localDevelopment) localEnvironment else ApiEnvironment.Production

internal expect fun platformDebugApiPreferences(scope: Scope): DebugApiPreferences
