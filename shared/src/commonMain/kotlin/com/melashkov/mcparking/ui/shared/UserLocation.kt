package com.melashkov.mcparking.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.mostAccurateBearing
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberDefaultOrientationProvider
import org.maplibre.compose.location.rememberUserLocationState

@Composable
fun UserLocation(
    camera: CameraState,
    locateRequest: Int,
) {
    val locationProvider =
        rememberDefaultLocationProvider()

    val orientationProvider =
        rememberDefaultOrientationProvider()

    val locationState =
        rememberUserLocationState(
            locationProvider = locationProvider,
            orientationProvider = orientationProvider,
        )

    LocationPuck(
        idPrefix = "user",
        location = locationState.location,
        bearing = locationState.mostAccurateBearing(),
        cameraState = camera,
    )

    LaunchedEffect(locateRequest) {
        if (locateRequest == 0) {
            return@LaunchedEffect
        }

        val location =
            snapshotFlow { locationState.location }
                .filterNotNull()
                .first()

        camera.animateTo(
            CameraPosition(
                target = location.position.value,
                zoom = 15.0,
            )
        )
    }
}