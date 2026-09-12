package com.melashkov.mcparking.di

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.scope.Scope

@OptIn(ExperimentalNativeApi::class)
internal actual fun platformApiUrlProvider(scope: Scope): ApiUrlProvider {
    val environment =
        if (Platform.isDebugBinary) {
            ApiEnvironment.LocalhostDevelopment
        } else {
            ApiEnvironment.Production
        }
    return apiUrlProvider(environment)
}
