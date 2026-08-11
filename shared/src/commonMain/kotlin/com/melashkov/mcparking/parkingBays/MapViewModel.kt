package com.melashkov.mcparking.parkingBays

import androidx.lifecycle.ViewModel
import com.melashkov.mcparking.data.LocationsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.ViewModelScope
import org.maplibre.compose.camera.CameraPosition

sealed interface MapUiEvent {
    data class MoveCamera(
        val position: CameraPosition,
        val animated: Boolean = true
    ) : MapUiEvent
}

@KoinViewModel
class MapViewModel(
    private val repository: LocationsRepository
) : ViewModel() {
    private val _events = Channel<MapUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()


    fun onMapCentreChanged(position: CameraPosition) {
        println("onMapCentreChanged $position")
    }

    val userName: String
        get() = repository.getUserName()
}