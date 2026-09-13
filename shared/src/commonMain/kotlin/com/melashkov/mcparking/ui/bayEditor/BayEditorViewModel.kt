package com.melashkov.mcparking.ui.bayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SubmitParkingBayUseCase
import com.melashkov.mcparking.ui.navigation.AppNavigationSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

sealed interface BayEditorEvent {
    data object Back : BayEditorEvent
    data object ClearSubmissionError : BayEditorEvent
    data class Submit(val submission: ParkingBaySubmission) : BayEditorEvent
    data object Done : BayEditorEvent
}

data class BayEditorUiState(
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submissionError: AppError? = null,
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

    private fun onEvent(event: BayEditorEvent) {
        when (event) {
            BayEditorEvent.Back -> appNavigationSink.back()
            BayEditorEvent.ClearSubmissionError -> clearSubmissionError()
            is BayEditorEvent.Submit -> submit(event.submission)
            BayEditorEvent.Done -> appNavigationSink.back()
        }
    }

    private fun clearSubmissionError() {
        if (_uiState.value.submissionError == null) return
        _uiState.update { it.copy(submissionError = null) }
    }

    private fun submit(submission: ParkingBaySubmission) {
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                isSubmitting = true,
                isSubmitted = false,
                submissionError = null,
            )
        }
        viewModelScope.launch {
            when (val result = submitParkingBay(submission)) {
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
}
