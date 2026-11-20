package com.melashkov.mcparking.di

import org.koin.core.scope.Scope
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

internal actual fun platformApiClientMetadata(scope: Scope): ApiClientMetadata {
    val preferences = NSUserDefaults.standardUserDefaults
    val installationId = preferences.stringForKey("api_installation_id") ?: NSUUID().UUIDString.lowercase().also {
        preferences.setObject(it, forKey = "api_installation_id")
    }
    return ApiClientMetadata(
        platform = "ios",
        appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown",
        appVersionCode = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "unknown",
        osVersion = UIDevice.currentDevice.systemVersion,
        installationId = installationId,
    )
}
