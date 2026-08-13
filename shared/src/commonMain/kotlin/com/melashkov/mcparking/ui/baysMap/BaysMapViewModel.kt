package com.melashkov.mcparking.ui.baysMap

import androidx.lifecycle.ViewModel
import com.melashkov.mcparking.data.LocationsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position

sealed interface MapUiEvent {
    data class MoveCamera(
        val position: CameraPosition,
        val animated: Boolean = true
    ) : MapUiEvent
}

@KoinViewModel
class BaysMapViewModel(
    private val repository: LocationsRepository
) : ViewModel() {

    val firstPosition =
        CameraPosition(target = Position(latitude = 45.521, longitude = -122.675), zoom = 13.0)

    private val _events = Channel<MapUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()


    fun onMapCentreChanged(position: CameraPosition) {
        println("onMapCentreChanged $position")
    }

    val userName: String
        get() = repository.getUserName()
}