package com.melashkov.mcparking.parkingBays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@Composable
fun ParkingBaysMap(
    vm: MapViewModel = viewModel()
) {
    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(target = Position(latitude = 45.521, longitude = -122.675), zoom = 13.0)
        )

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is MapUiEvent.MoveCamera ->
                    camera.animateTo(event.position)
            }
        }
    }

    LaunchedEffect(camera) {
        snapshotFlow { camera.position }
            .collect { vm.onMapCentreChanged(it) }
    }

    MaplibreMap(
        baseStyle = BaseStyle.Uri(
            "https://tiles.openfreemap.org/styles/liberty"
        ),
        cameraState = camera
    )
}