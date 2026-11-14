package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider =
    apiUrlProvider(scope.get<DebugApiBackend>().activeEnvironment(ApiEnvironment.LocalhostDevelopment))
