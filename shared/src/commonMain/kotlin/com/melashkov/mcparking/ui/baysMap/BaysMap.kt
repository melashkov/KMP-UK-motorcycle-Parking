package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.permissions.rememberLocationPermissionState
import com.melashkov.mcparking.ui.shared.MapAttributionSpace
import com.melashkov.mcparking.ui.shared.ParkingMapOverlay
import com.melashkov.mcparking.ui.shared.UserLocation
import com.melashkov.mcparking.ui.support.MapOverflowMenu
import com.melashkov.mcparking.ui.theme.parkingMapStyle
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.spatialk.geojson.Position
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.accessibility_my_location
import ukmotorcycleparking.shared.generated.resources.places_search_title

@Composable
fun BaysMap(
    uiState: MapUiState,
) {
    ParkingBayDetailsScaffold(
        bay = uiState.selectedBay,
        onDismissRequest = {
            uiState.eventSink(MapUiEvent.DismissBayDetails)
        },
        onNavigate = {
            uiState.eventSink(MapUiEvent.NavigateToBay(it))
        },
        onShare = {
            uiState.eventSink(MapUiEvent.ShareBay(it))
        },
        onSuggestEdit = {
            uiState.eventSink(MapUiEvent.SuggestEdit(it))
        },
        onStreetView = {
            uiState.eventSink(MapUiEvent.OpenStreetView(it))
        },
    ) {
        BaysMapContent(uiState)
    }
}

@Composable
private fun BaysMapContent(
    uiState: MapUiState,
) {
    val coroutineScope = rememberCoroutineScope()
    val destination = remember { mutableStateOf<Place?>(null) }
    val locationPermission = rememberLocationPermissionState()
    var locateWhenGranted by remember { mutableStateOf(false) }
    var locateRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(locationPermission.granted, locationPermission) {
        if (locationPermission.granted && locateWhenGranted) {
            locateWhenGranted = false
            locateRequest++
        }
    }

    val cameraState = rememberMapState(
        baseStyle = parkingMapStyle(),
        initialCameraPosition = InitialCameraPosition,
    ) {
        val cameraState = checkNotNull(LocalMapState.current)
        destination.value?.let { DestinationMarker(it) }
        ParkingBayMarkers(
            markers = uiState.parkingBays,
            onMarkerClick = {
                uiState.eventSink(MapUiEvent.SelectedBay(it))
            },
            onClusterClick = { clusterPosition ->
                coroutineScope.launch {
                    cameraState.animateCamera(
                        CameraUpdate(
                            center = clusterPosition,
                            zoom = (cameraState.cameraPosition.zoom + ClusterZoomIncrement)
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
    val placeSearch = rememberMapPlaceSearchState(cameraState, uiState.eventSink, destination)

    LaunchedEffect(cameraState) {
        cameraState.awaitViewport()
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
            state = cameraState,
            overlay = { ParkingMapOverlay(cameraState) },
        )

        SmallFloatingActionButton(
            onClick = placeSearch::openSearch,
            modifier = Modifier.align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.places_search_title))
        }

        if (placeSearch.isSearchVisible) {
            PlacesSearchScreen(
                near = cameraState.cameraPosition.center.toGeoCoordinate(),
                onDismiss = placeSearch::dismissSearch,
                onSelect = placeSearch::selectPlace,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
                .padding(bottom = MapAttributionSpace + 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapSearchMessage(
                error = uiState.error,
                requiresZoom = uiState.requiresZoomToSearch,
                showEmptySearch = uiState.showEmptySearch,
                onRetry = {
                    uiState.eventSink(MapUiEvent.SearchCurrentArea)
                },
                onDismiss = {
                    uiState.eventSink(MapUiEvent.DismissSearchMessage)
                },
            )

            if (uiState.error != null || uiState.requiresZoomToSearch || uiState.showEmptySearch) {
                Spacer(Modifier.height(8.dp))
            }

            SearchThisAreaButton(
                show = uiState.isLoading || (uiState.showSearchThisArea &&
                    uiState.error == null &&
                    !uiState.requiresZoomToSearch),
                isLoading = uiState.isLoading,
                onClick = {
                    uiState.eventSink(
                        if (uiState.isLoading) MapUiEvent.CancelSearch else MapUiEvent.SearchCurrentArea,
                    )
                },
            )
        }

        MapOverflowMenu(
            onAddBay = {
                uiState.eventSink(MapUiEvent.AddBay(cameraState.cameraPosition.center.toGeoCoordinate()))
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        )

        SmallFloatingActionButton(
            onClick = {
                if (locationPermission.granted) {
                    locateRequest++
                } else {
                    locateWhenGranted = true
                    locationPermission.request()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
                .padding(bottom = MapAttributionSpace + 16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(Res.string.accessibility_my_location),
            )
        }
    }
}

private val InitialCameraPosition = CameraPosition(
    center = Position(
        latitude = 51.512682148762195,
        longitude = -0.0904589182234332,
    ),
    zoom = 13.0,
)

private const val ClusterZoomIncrement = 2.0
private const val MaxClusterTapZoom = 15.0



internal fun MapState.currentViewport(): MapViewport? {
    val viewport = viewport ?: return null
    val bounds = viewport.visibleBounds.toBoundingBox()

    return MapViewport(
        center = viewport.cameraPosition.center.toGeoCoordinate(),
        bounds = GeoBounds(
            west = bounds.west,
            south = bounds.south,
            east = bounds.east,
            north = bounds.north
        ),
        zoom = viewport.cameraPosition.zoom,
    )
}

private fun Position.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
