package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.permissions.rememberLocationPermissionState
import com.melashkov.mcparking.ui.shared.UserLocation
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@OptIn(FlowPreview::class)
@Composable
fun BaysMap(
    vm: BaysMapViewModel = koinViewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val cameraState = rememberCameraState(firstPosition = vm.firstPosition)

    val locationPermission = rememberLocationPermissionState()
    var locateWhenGranted by remember { mutableStateOf(false) }
    var locateRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(locationPermission.granted, locationPermission) {
        if (locationPermission.granted && locateWhenGranted) {
            locateWhenGranted = false
            locateRequest++
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is MapUiEvent.MoveCamera ->
                    cameraState.animateTo(event.position)
            }
        }
    }

    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.isCameraMoving }
            .dropWhile { !it }
            .filter { !it }
            .collect {
                cameraState.currentViewport()
                    ?.let(vm::onMapViewportChanged)
            }
    }

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(
                "https://tiles.openfreemap.org/styles/liberty"
            ),
            cameraState = cameraState
        ) {
            ParkingBayMarkers(uiState.parkingBays)

            if (locationPermission.granted) {
                UserLocation(
                    camera = cameraState,
                    locateRequest = locateRequest,
                )
            }
        }

        SearchThisAreaButton(
            show = uiState.showSearchThisArea,
            onClick = vm::searchCurrentArea,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 12.dp),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp),
            color = Color.White.copy(alpha = 0.9f),
        ) {
            Text(
                text = "Zoom: ${cameraState.position.zoom}\nBays: ${uiState.parkingBays.size}\nClusters to zoom: 14",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.Black,
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                if (locationPermission.granted) {
                    locateRequest++
                } else {
                    locateWhenGranted = true
                    locationPermission.request()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My location",
            )
        }
    }
}


private fun CameraState.currentViewport(): MapViewport? {
    val bounds = projection?.queryVisibleBoundingBox()
        ?: return null

    return MapViewport(
        center = position.target.toGeoCoordinate(),
        bounds = GeoBounds(
            west = bounds.west,
            south = bounds.south,
            east = bounds.east,
            north = bounds.north
        ),
        zoom = position.zoom,
    )
}

private fun Position.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
