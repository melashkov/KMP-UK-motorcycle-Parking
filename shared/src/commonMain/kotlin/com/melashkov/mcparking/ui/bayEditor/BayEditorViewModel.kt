package com.melashkov.mcparking.ui.bayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.DataError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SubmitParkingBayUseCase
import com.melashkov.mcparking.ui.navigation.AppNavigationSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

enum class BayEditorMode {
    Add,
    Edit,
}

data class BayEditorInitialData(
    val mode: BayEditorMode,
    val parentId: String? = null,
    val title: String = "",
    val description: String = "",
    val type: ParkingType? = null,
    val location: GeoCoordinate,
)

sealed interface BayEditorEvent {
    data object Back : BayEditorEvent
    data class TitleChanged(val value: String) : BayEditorEvent
    data class DescriptionChanged(val value: String) : BayEditorEvent
    data class TypeChanged(val value: ParkingType) : BayEditorEvent
    data object ChooseLocation : BayEditorEvent
    data object DismissLocationPicker : BayEditorEvent
    data class LocationSelected(val value: GeoCoordinate) : BayEditorEvent
    data object Submit : BayEditorEvent
    data object Done : BayEditorEvent
}

data class BayEditorUiState(
    val isInitialized: Boolean = false,
    val mode: BayEditorMode = BayEditorMode.Add,
    val title: String = "",
    val description: String = "",
    val type: ParkingType? = null,
    val location: GeoCoordinate? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val typeError: String? = null,
    val locationError: String? = null,
    val isLocationPickerOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submissionError: DataError? = null,
    val eventSink: (BayEditorEvent) -> Unit = {},
)

@KoinViewModel
class BayEditorViewModel(
    private val submitParkingBay: SubmitParkingBayUseCase,
    private val appNavigationSink: AppNavigationSink,
) : ViewModel() {
    private val eventSink: (BayEditorEvent) -> Unit = ::onEvent
    private val _uiState = MutableStateFlow(BayEditorUiState(eventSink = eventSink))
    val uiState = _uiState.asStateFlow()

    private var parentId: String? = null

    fun initialize(initialData: BayEditorInitialData) {
        if (_uiState.value.isInitialized) return

        parentId = initialData.parentId
        _uiState.value = BayEditorUiState(
            isInitialized = true,
            mode = initialData.mode,
            title = initialData.title,
            description = initialData.description,
            type = initialData.type,
            location = initialData.location,
            eventSink = eventSink,
        )
    }

    private fun onEvent(event: BayEditorEvent) {
        when (event) {
            BayEditorEvent.Back -> onBack()
            is BayEditorEvent.TitleChanged -> updateTitle(event.value)
            is BayEditorEvent.DescriptionChanged -> updateDescription(event.value)
            is BayEditorEvent.TypeChanged -> updateType(event.value)
            BayEditorEvent.ChooseLocation -> {
                _uiState.update { it.copy(isLocationPickerOpen = true) }
            }
            BayEditorEvent.DismissLocationPicker -> {
                _uiState.update { it.copy(isLocationPickerOpen = false) }
            }
            is BayEditorEvent.LocationSelected -> {
                _uiState.update {
                    it.copy(
                        location = event.value,
                        locationError = null,
                        isLocationPickerOpen = false,
                    )
                }
            }
            BayEditorEvent.Submit -> submit()
            BayEditorEvent.Done -> appNavigationSink.back()
        }
    }

    private fun onBack() {
        if (_uiState.value.isLocationPickerOpen) {
            _uiState.update { it.copy(isLocationPickerOpen = false) }
        } else {
            appNavigationSink.back()
        }
    }

    private fun updateTitle(value: String) {
        if (value.length > TITLE_MAX_LENGTH) return
        _uiState.update {
            it.copy(
                title = value,
                titleError = null,
                submissionError = null,
            )
        }
    }

    private fun updateDescription(value: String) {
        if (value.length > DESCRIPTION_MAX_LENGTH) return
        _uiState.update {
            it.copy(
                description = value,
                descriptionError = null,
                submissionError = null,
            )
        }
    }

    private fun updateType(value: ParkingType) {
        if (!value.isAvailableFor(_uiState.value.mode)) return
        _uiState.update {
            it.copy(
                type = value,
                typeError = null,
                submissionError = null,
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isInitialized || state.isSubmitting) return

        val title = state.title.trim()
        val description = state.description.trim()
        val titleError = when {
            title.isEmpty() -> "Enter a name for this parking bay"
            title.length > TITLE_MAX_LENGTH -> "Use $TITLE_MAX_LENGTH characters or fewer"
            else -> null
        }
        val descriptionError =
            if (description.length > DESCRIPTION_MAX_LENGTH) {
                "Use $DESCRIPTION_MAX_LENGTH characters or fewer"
            } else {
                null
            }
        val typeError = if (state.type?.isAvailableFor(state.mode) == true) {
            null
        } else {
            "Choose a parking type"
        }
        val locationError = if (state.location == null) "Choose a location" else null

        _uiState.update {
            it.copy(
                title = title,
                description = description,
                titleError = titleError,
                descriptionError = descriptionError,
                typeError = typeError,
                locationError = locationError,
                submissionError = null,
            )
        }

        val type = state.type?.takeIf { it.isAvailableFor(state.mode) } ?: return
        val location = state.location ?: return
        if (titleError != null || descriptionError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (
                val result = submitParkingBay(
                    ParkingBaySubmission(
                        parentId = parentId,
                        title = title,
                        description = description,
                        type = type,
                        position = location,
                    ),
                )
            ) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            submissionError = null,
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = result.error,
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TITLE_MAX_LENGTH = 60
        const val DESCRIPTION_MAX_LENGTH = 255

        val EditableParkingTypes = listOf(
            ParkingType.FREE,
            ParkingType.PAY,
            ParkingType.PERMIT,
            ParkingType.UNCATEGORISED,
        )
    }
}

private fun ParkingType.isAvailableFor(mode: BayEditorMode): Boolean =
    this in BayEditorViewModel.EditableParkingTypes ||
        (mode == BayEditorMode.Edit && this == ParkingType.INACTIVE)
