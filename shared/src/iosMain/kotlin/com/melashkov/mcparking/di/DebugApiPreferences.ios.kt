package com.melashkov.mcparking.di

import kotlin.native.Platform
import org.koin.core.scope.Scope
import platform.Foundation.NSUserDefaults

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
internal actual fun platformDebugApiPreferences(scope: Scope): DebugApiPreferences =
    object : DebugApiPreferences {
        override val enabled = Platform.isDebugBinary
        override var localDevelopment: Boolean
            get() = enabled && NSUserDefaults.standardUserDefaults.boolForKey("debug_api_local_development")
            set(value) {
                if (enabled) NSUserDefaults.standardUserDefaults.setBool(value, forKey = "debug_api_local_development")
            }
    }
