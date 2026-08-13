package com.melashkov.mcparking.permissions

import androidx.compose.runtime.Composable

interface LocationPermissionState {
    val granted: Boolean

    fun request()
}

@Composable
expect fun rememberLocationPermissionState(): LocationPermissionState