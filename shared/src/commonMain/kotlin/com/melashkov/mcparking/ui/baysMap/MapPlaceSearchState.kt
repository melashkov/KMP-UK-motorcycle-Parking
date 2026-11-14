package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.MapState
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.wrapped
import kotlin.math.abs

/** UI state for opening place search, displaying a destination and moving to it. */
internal class MapPlaceSearchState(
    private val scope: CoroutineScope,
    private val onEvent: (MapUiEvent) -> Unit,
) {
    var isSearchVisible by mutableStateOf(false)
        private set
    var destination by mutableStateOf<Place?>(null)
        private set

    private var navigationJob: Job? = null

    fun openSearch() {
        isSearchVisible = true
    }

    fun dismissSearch() {
        isSearchVisible = false
    }

    fun cancelPendingNavigation() {
        navigationJob?.cancel()
        navigationJob = null
    }

    fun selectPlace(place: Place, moveCamera: suspend (Place) -> MapViewport?) {
        cancelPendingNavigation()
        isSearchVisible = false
        destination = place
        onEvent(MapUiEvent.CancelSearch)
        onEvent(MapUiEvent.DismissBayDetails)
        navigationJob = scope.launch {
            val viewport = moveCamera(place)
            ensureActive()
            if (viewport != null && viewport.matchesDestination(place)) {
                onEvent(MapUiEvent.SearchDestinationArea(viewport))
            }
        }
    }
}

@Composable
internal fun rememberMapPlaceSearchState(
    onEvent: (MapUiEvent) -> Unit,
): MapPlaceSearchState {
    val scope = rememberCoroutineScope()
    return remember(scope, onEvent) { MapPlaceSearchState(scope, onEvent) }
}

internal suspend fun MapState.moveToDestination(place: Place): MapViewport? {
    animateCamera(
        CameraUpdate(
            center = Position(place.longitude, place.latitude),
            zoom = place.destinationZoom(),
        ),
    )
    snapshotFlow { isCameraMoving }.first { !it }
    return currentViewport()
}

private fun MapViewport.matchesDestination(place: Place): Boolean {
    val longitudeDifference = Position(center.longitude - place.longitude, 0.0).wrapped().longitude
    return abs(center.latitude - place.latitude) <= 0.00001 &&
        abs(longitudeDifference) <= 0.00001 &&
        abs(zoom - place.destinationZoom()) <= 0.001
}
