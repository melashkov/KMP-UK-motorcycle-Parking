package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

class ApiDebugLogger(
    val enabled: Boolean = false,
    private val write: (String) -> Unit = {},
) {
    fun log(message: String) {
        if (enabled) write(message)
    }
}

internal expect fun platformApiDebugLogger(scope: Scope): ApiDebugLogger
