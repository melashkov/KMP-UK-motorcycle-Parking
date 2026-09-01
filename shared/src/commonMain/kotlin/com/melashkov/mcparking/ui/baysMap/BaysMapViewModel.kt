package com.melashkov.mcparking.ui.baysMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.DataError
import com.melashkov.mcparking.domain.usecases.SearchParkingBaysUseCase
import com.melashkov.mcparking.domain.usecases.SearchParkingResult
import com.melashkov.mcparking.domain.usecases.ShouldShowSearchThisAreaUseCase
import com.melashkov.mcparking.ui.bayEditor.navigation.AddBayRoute
import com.melashkov.mcparking.ui.bayEditor.navigation.EditBayRoute
import com.melashkov.mcparking.ui.navigation.AppNavigationSink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
sealed interface MapUiEvent {
    data class InitialViewport(val viewport: MapViewport) : MapUiEvent
    data class ViewportChanged(val viewport: MapViewport) : MapUiEvent
    data object SearchCurrentArea : MapUiEvent
    data object AddBay : MapUiEvent
    data class NavigateToBay(val bay: ParkingBay) : MapUiEvent
    data class ShareBay(val bay: ParkingBay) : MapUiEvent
    data class SelectedBay(val bay: ParkingBay) : MapUiEvent
    data class SuggestEdit(val bay: ParkingBay) : MapUiEvent
    data class OpenStreetView(val bay: ParkingBay) : MapUiEvent
    data object DismissBayDetails : MapUiEvent
}

data class MapUiState(
    val parkingBays: ImmutableList<ParkingBay> = persistentListOf(),
    val selectedBay: ParkingBay? = null,
    val isLoading: Boolean = false,
    val showSearchThisArea: Boolean = false,
    val error: MapUiError? = null,
    val currentViewport: MapViewport? = null,
    val eventSink: (MapUiEvent) -> Unit = {},
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
    private val shouldShowSearchThisArea: ShouldShowSearchThisAreaUseCase,
    private val appNavigationSink: AppNavigationSink,
) : ViewModel() {

    private val eventSink: (MapUiEvent) -> Unit = ::onEvent

    private val _uiState = MutableStateFlow(MapUiState(eventSink = eventSink))
    val uiState = _uiState.asStateFlow()

    private var hasSearchedInitialArea = false

    private fun onEvent(event: MapUiEvent) {
        when (event) {
            is MapUiEvent.InitialViewport -> searchInitialArea(event.viewport)
            is MapUiEvent.ViewportChanged -> onMapViewportChanged(event.viewport)
            MapUiEvent.SearchCurrentArea -> searchCurrentArea()
            MapUiEvent.AddBay -> appNavigationSink.navigate(AddBayRoute)

            is MapUiEvent.SelectedBay -> {
                _uiState.update { it.copy(selectedBay = event.bay) }
            }

            MapUiEvent.DismissBayDetails -> {
                _uiState.update { it.copy(selectedBay = null) }
            }

            is MapUiEvent.NavigateToBay -> {
                appNavigationSink.launch(event.bay.directionsAction())
            }

            is MapUiEvent.ShareBay -> {
                appNavigationSink.launch(event.bay.shareAction())
            }

            is MapUiEvent.SuggestEdit -> {
                appNavigationSink.navigate(EditBayRoute(event.bay.id))
            }

            is MapUiEvent.OpenStreetView -> {
                appNavigationSink.launch(event.bay.streetViewAction())
            }
        }
    }

    private fun searchInitialArea(viewport: MapViewport) {
        if (hasSearchedInitialArea) {
            onMapViewportChanged(viewport)
            return
        }

        hasSearchedInitialArea = true
        _uiState.update {
            it.copy(
                currentViewport = viewport,
                showSearchThisArea = false,
            )
        }
        searchCurrentArea()
    }

    private fun onMapViewportChanged(viewport: MapViewport) {
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

    private fun searchCurrentArea() {
        if (_uiState.value.isLoading) return

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
                    it.copy(
                        isLoading = false,
                        showSearchThisArea = true,
                        error = MapUiError.Unknown,
                    )
                }
                return@launch
            }
            when (val result = searchParkingBays(viewport)) {
                is SearchParkingResult.Success -> {
                    _uiState.update {
                        val bays = result.bays.toImmutableList()
                        it.copy(
                            parkingBays = bays,
                            selectedBay = it.selectedBay?.let { selected ->
                                bays.firstOrNull { bay -> bay.id == selected.id }
                            },
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
                            showSearchThisArea = true,
                            error = MapUiError.ZoomInToSearch,
                        )
                    }
                }

                is SearchParkingResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showSearchThisArea = true,
                            error = result.error.toUiError(),
                        )
                    }
                }
            }
        }
    }
}

private fun DataError.toUiError(): MapUiError = when (this) {
    DataError.Offline -> MapUiError.Offline
    DataError.ServerUnavailable -> MapUiError.ServerUnavailable
    DataError.Unauthorized -> MapUiError.Unauthorized
    else -> MapUiError.Unknown
}
