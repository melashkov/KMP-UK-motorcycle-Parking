package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
@Composable
internal actual fun rememberApiUrlProvider(): ApiUrlProvider {
    val environment =
        if (Platform.isDebugBinary) {
            ApiEnvironment.LocalhostDevelopment
        } else {
            ApiEnvironment.Production
        }
    return remember(environment) { apiUrlProvider(environment) }
}
