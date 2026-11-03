package com.melashkov.mcparking.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.layers.LocationIndicatorLayer
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberLocationState
import org.maplibre.compose.map.MapState

@Composable
fun UserLocation(
    camera: MapState,
    locateRequest: Int,
) {
    val locationProvider =
        rememberDefaultLocationProvider()

    val headingProvider =
        rememberDefaultHeadingProvider()

    val locationState =
        rememberLocationState(
            provider = locationProvider,
            headingProvider = headingProvider,
        )

    LocationIndicatorLayer(
        id = "user",
        locationState = locationState,
    )

    LaunchedEffect(locateRequest) {
        if (locateRequest == 0) {
            return@LaunchedEffect
        }

        val location =
            snapshotFlow { locationState.lastLocation }
                .filterNotNull()
                .first()

        camera.animateCamera(
            CameraUpdate(
                center = location.position,
                zoom = 15.0,
            )
        )
    }
}