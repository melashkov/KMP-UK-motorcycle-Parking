package com.melashkov.mcparking.di

import org.koin.core.scope.Scope
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

internal actual fun platformApiClientMetadata(scope: Scope, installationId: String): ApiClientMetadata =
    ApiClientMetadata(
        platform = "ios",
        appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown",
        appVersionCode = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "unknown",
        osVersion = UIDevice.currentDevice.systemVersion,
        installationId = installationId,
    )
