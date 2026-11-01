package com.melashkov.mcparking.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SendFeedbackUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FeedbackViewModel(private val sendFeedback: SendFeedbackUseCase) : ViewModel() {
    private val eventSink: (FeedbackEvent) -> Unit = ::onEvent
    private val _uiState = MutableStateFlow(FeedbackUiState(eventSink = eventSink))
    val uiState = _uiState.asStateFlow()

    private fun onEvent(event: FeedbackEvent) {
        when (event) {
            is FeedbackEvent.MessageChanged -> edit { it.copy(message = event.value) }
            is FeedbackEvent.EmailChanged -> edit { it.copy(email = event.value) }
            FeedbackEvent.Submit -> submit()
            FeedbackEvent.Reset -> reset()
        }
    }

    private fun edit(transform: (FeedbackUiState) -> FeedbackUiState) {
        _uiState.update {
            if (it.isEditable) transform(it).copy(submissionStatus = FeedbackSubmissionStatus.Idle) else it
        }
    }

    private fun reset() {
        _uiState.update { if (it.isSending) it else FeedbackUiState(eventSink = eventSink) }
    }

    private fun submit() {
        val draft = _uiState.value
        if (!draft.canSubmit) return
        _uiState.value = draft.copy(submissionStatus = FeedbackSubmissionStatus.Sending)
        viewModelScope.launch {
            _uiState.value = when (val result = sendFeedback(draft.message, draft.email)) {
                is DataResult.Success -> FeedbackUiState(
                    submissionStatus = FeedbackSubmissionStatus.Sent,
                    eventSink = eventSink,
                )
                is DataResult.Failure -> draft.copy(submissionStatus = FeedbackSubmissionStatus.Failed(result.error))
            }
        }
    }
}
