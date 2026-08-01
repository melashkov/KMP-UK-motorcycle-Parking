package com.melashkov.mcparking.parkingBays

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.maplibre.compose.camera.CameraPosition

sealed interface MapUiEvent {
    data class MoveCamera(
        val position: CameraPosition,
        val animated: Boolean = true
    ) : MapUiEvent
}


class MapViewModel : ViewModel() {
    private val _events = Channel<MapUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()


    fun onMapCentreChanged(position: CameraPosition) {
        println("onMapCentreChanged $position")
    }

}