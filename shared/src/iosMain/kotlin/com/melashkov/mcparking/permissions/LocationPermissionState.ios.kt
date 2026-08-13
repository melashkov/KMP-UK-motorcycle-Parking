@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.melashkov.mcparking.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

private class IosLocationPermissionState(
    private val grantedState: State<Boolean>,
    private val manager: CLLocationManager,
) : LocationPermissionState {

    override val granted: Boolean
        get() = grantedState.value

    override fun request() {
        if (
            manager.authorizationStatus ==
            kCLAuthorizationStatusNotDetermined
        ) {
            manager.requestWhenInUseAuthorization()
        }
    }
}

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    val manager = remember {
        CLLocationManager()
    }

    val granted = remember {
        mutableStateOf(
            manager.authorizationStatus.isGranted()
        )
    }

    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {

            override fun locationManagerDidChangeAuthorization(
                manager: CLLocationManager,
            ) {
                granted.value =
                    manager.authorizationStatus.isGranted()
            }
        }
    }

    DisposableEffect(manager, delegate) {
        manager.delegate = delegate

        onDispose {
            manager.delegate = null
        }
    }

    return remember(manager, granted) {
        IosLocationPermissionState(
            grantedState = granted,
            manager = manager,
        )
    }
}

private fun CLAuthorizationStatus.isGranted(): Boolean =
    this == kCLAuthorizationStatusAuthorizedWhenInUse ||
            this == kCLAuthorizationStatusAuthorizedAlways