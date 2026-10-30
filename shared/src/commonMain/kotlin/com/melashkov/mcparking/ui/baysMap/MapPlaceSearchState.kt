package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.Position

/** UI state for opening place search, displaying a destination and moving to it. */
internal class MapPlaceSearchState(
    private val scope: CoroutineScope,
    private val moveCamera: suspend (Place) -> MapViewport?,
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

    fun selectPlace(place: Place) {
        navigationJob?.cancel()
        isSearchVisible = false
        destination = place
        onEvent(MapUiEvent.CancelSearch)
        onEvent(MapUiEvent.DismissBayDetails)
        navigationJob = scope.launch {
            val viewport = moveCamera(place)
            // Do not search for an old selection if its camera animation finishes late.
            ensureActive()
            if (viewport != null) onEvent(MapUiEvent.SearchDestinationArea(viewport))
        }
    }
}

@Composable
internal fun rememberMapPlaceSearchState(
    cameraState: CameraState,
    onEvent: (MapUiEvent) -> Unit,
): MapPlaceSearchState {
    val scope = rememberCoroutineScope()
    val currentOnEvent by rememberUpdatedState(onEvent)
    return remember(cameraState, scope) {
        MapPlaceSearchState(
            scope = scope,
            moveCamera = { place ->
                cameraState.animateTo(cameraState.position.copy(
                    target = Position(place.longitude, place.latitude),
                    zoom = place.destinationZoom(),
                ))
                cameraState.currentViewport()
            },
            onEvent = { currentOnEvent(it) },
        )
    }
}
