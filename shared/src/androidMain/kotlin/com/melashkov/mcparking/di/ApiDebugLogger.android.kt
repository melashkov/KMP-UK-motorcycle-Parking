package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import org.koin.core.scope.Scope

internal actual fun platformIsDebugBuild(scope: Scope): Boolean =
    scope.get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

internal actual fun platformApiDebugLogger(scope: Scope): ApiDebugLogger =
    ApiDebugLogger(platformIsDebugBuild(scope)) { Log.d("MCParkingApi", it) }
