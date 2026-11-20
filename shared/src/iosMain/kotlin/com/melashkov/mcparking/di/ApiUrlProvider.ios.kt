package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider =
    apiUrlProvider(ApiEnvironment.Production)
