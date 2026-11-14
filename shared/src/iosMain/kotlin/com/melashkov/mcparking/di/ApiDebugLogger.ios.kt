package com.melashkov.mcparking.di

import kotlin.native.Platform
import org.koin.core.scope.Scope

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
internal actual fun platformIsDebugBuild(scope: Scope): Boolean = Platform.isDebugBinary

internal actual fun platformApiDebugLogger(scope: Scope): ApiDebugLogger =
    ApiDebugLogger(platformIsDebugBuild(scope)) { println("MCParkingApi: $it") }
