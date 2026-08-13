package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.permissions.rememberLocationPermissionState
import com.melashkov.mcparking.ui.shared.UserLocation
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle


@Composable
fun BaysMap(
    vm: BaysMapViewModel = koinViewModel()
) {
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
        snapshotFlow { cameraState.position }
            .collect(vm::onMapCentreChanged)
    }

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(
                "https://tiles.openfreemap.org/styles/liberty"
            ),
            cameraState = cameraState
        ) {
            if (locationPermission.granted) {
                UserLocation(
                    camera = cameraState,
                    locateRequest = locateRequest,
                )
            }
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
