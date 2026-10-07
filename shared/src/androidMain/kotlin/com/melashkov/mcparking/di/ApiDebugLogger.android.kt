package com.melashkov.mcparking.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import org.koin.core.scope.Scope

internal actual fun platformApiDebugLogger(scope: Scope): ApiDebugLogger {
    val context = scope.get<Context>()
    val enabled = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    return ApiDebugLogger(enabled) { Log.d("MCParkingApi", it) }
}
