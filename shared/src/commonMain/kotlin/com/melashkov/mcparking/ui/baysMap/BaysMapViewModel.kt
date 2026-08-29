package com.melashkov.mcparking.ui.baysMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.DataError
import com.melashkov.mcparking.domain.usecases.SearchParkingBaysUseCase
import com.melashkov.mcparking.domain.usecases.SearchParkingResult
import com.melashkov.mcparking.domain.usecases.ShouldShowSearchThisAreaUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position

sealed interface MapUiEvent {
    data class MoveCamera(
        val position: CameraPosition, val animated: Boolean = true
    ) : MapUiEvent
}

data class MapUiState(
    val parkingBays: List<ParkingBay> = emptyList(),
    val isLoading: Boolean = false,
    val showSearchThisArea: Boolean = false,
    val error: MapUiError? = null,
    val currentViewport: MapViewport? = null
)

sealed interface MapUiError {
    data object ZoomInToSearch : MapUiError
    data object Offline : MapUiError
    data object ServerUnavailable : MapUiError
    data object Unauthorized : MapUiError
    data object Unknown : MapUiError
}

@KoinViewModel
class BaysMapViewModel(
    private val searchParkingBays: SearchParkingBaysUseCase,
    private val shouldShowSearchThisArea: ShouldShowSearchThisAreaUseCase
) : ViewModel() {

    val firstPosition =
        CameraPosition(target = Position(latitude = 51.512682148762195, longitude = -0.0904589182234332), zoom = 13.0)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<MapUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onMapViewportChanged(viewport: MapViewport) {
        _uiState.update { state ->
            state.copy(
                currentViewport  = viewport,
                showSearchThisArea = shouldShowSearchThisArea(
                    viewport = viewport,
                    lastSearchedViewport = state.currentViewport,
                ),
            )
        }
    }

    fun searchCurrentArea() {
        _uiState.update {
            it.copy(showSearchThisArea = false)
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }
            val viewport = _uiState.value.currentViewport
            if (viewport == null) {
                _uiState.update {
                    it.copy(error = MapUiError.Unknown)
                }
                return@launch
            }
            when (val result = searchParkingBays(viewport)) {
                is SearchParkingResult.Success -> {
                    _uiState.update {
                        it.copy(
                            parkingBays = result.bays,
                            isLoading = false,
                            showSearchThisArea = false,
                            error = null,
                        )
                    }
                }

                SearchParkingResult.AreaTooLarge -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = MapUiError.ZoomInToSearch,
                        )
                    }
                }

                is SearchParkingResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.toUiError(),
                        )
                    }
                }
            }
        }
    }/*
fun onMapRegionMoved() {
    _uiState.update {
        it.copy(showSearchThisArea = true)
    }
}
 */
}

private fun DataError.toUiError(): MapUiError = when (this) {
    DataError.Offline -> MapUiError.Offline

    DataError.ServerUnavailable -> MapUiError.ServerUnavailable

    DataError.Unauthorized -> MapUiError.Unauthorized

    else -> MapUiError.Unknown
}