package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.permissions.rememberLocationPermissionState
import com.melashkov.mcparking.ui.shared.UserLocation
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@OptIn(FlowPreview::class)
@Composable
fun BaysMap(
    uiState: MapUiState
) {
    val cameraState = rememberCameraState(firstPosition = InitialCameraPosition)
    val coroutineScope = rememberCoroutineScope()

    val locationPermission = rememberLocationPermissionState()
    var locateWhenGranted by remember { mutableStateOf(false) }
    var locateRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(locationPermission.granted, locationPermission) {
        if (locationPermission.granted && locateWhenGranted) {
            locateWhenGranted = false
            locateRequest++
        }
    }

    LaunchedEffect(cameraState) {
        cameraState.awaitProjection()
        cameraState.currentViewport()
            ?.let { uiState.eventSink(MapUiEvent.InitialViewport(it)) }

        snapshotFlow { cameraState.isCameraMoving }
            .dropWhile { !it }
            .filter { !it }
            .collect {
                cameraState.currentViewport()
                    ?.let { uiState.eventSink(MapUiEvent.ViewportChanged(it)) }
            }
    }

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(
                "https://tiles.openfreemap.org/styles/liberty"
            ),
            cameraState = cameraState
        ) {
            ParkingBayMarkers(
                markers = uiState.parkingBays,
                onMarkerClick = {
                    uiState.eventSink(MapUiEvent.SelectedBay(it))
                },
                onClusterClick = { clusterPosition ->
                    coroutineScope.launch {
                        cameraState.animateTo(
                            cameraState.position.copy(
                                target = clusterPosition,
                                zoom = (cameraState.position.zoom + ClusterZoomIncrement)
                                    .coerceAtMost(MaxClusterTapZoom),
                            ),
                        )
                    }
                },
            )

            if (locationPermission.granted) {
                UserLocation(
                    camera = cameraState,
                    locateRequest = locateRequest,
                )
            }
        }

        SearchThisAreaButton(
            show = uiState.showSearchThisArea,
            onClick = {
                uiState.eventSink(MapUiEvent.SearchCurrentArea)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 12.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (locationPermission.granted) {
                        locateRequest++
                    } else {
                        locateWhenGranted = true
                        locationPermission.request()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My location",
                )
            }

            ExtendedFloatingActionButton(
                onClick = {
                    uiState.eventSink(MapUiEvent.AddBay)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = null,
                    )
                },
                text = { Text("Add bay") },
            )
        }
    }

    uiState.selectedBay?.let { bay ->
        ParkingBayDetailsSheet(
            bay = bay,
            onDismissRequest = {
                uiState.eventSink(MapUiEvent.DismissBayDetails)
            },
            onNavigate = {
                uiState.eventSink(MapUiEvent.NavigateToBay(bay))
            },
            onShare = {
                uiState.eventSink(MapUiEvent.ShareBay(bay))
            },
            onSuggestEdit = {
                uiState.eventSink(MapUiEvent.SuggestEdit(bay))
            },
            onStreetView = {
                uiState.eventSink(MapUiEvent.OpenStreetView(bay))
            },
        )
    }
}

private val InitialCameraPosition = CameraPosition(
    target = Position(
        latitude = 51.512682148762195,
        longitude = -0.0904589182234332,
    ),
    zoom = 13.0,
)

private const val ClusterZoomIncrement = 2.0
private const val MaxClusterTapZoom = 15.0


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
