package com.melashkov.mcparking.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.di.ApiSubmissionBackend
import com.melashkov.mcparking.di.DebugApiBackend
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SendFeedbackUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class FeedbackViewModel(
    private val sendFeedback: SendFeedbackUseCase,
    private val backend: DebugApiBackend,
) : ViewModel() {
    private val eventSink: (FeedbackEvent) -> Unit = ::onEvent
    private val _uiState = MutableStateFlow(FeedbackUiState(eventSink = eventSink))
    val uiState = _uiState.asStateFlow()
    private var draftBackend = backend.selection.value
    private var submissionJob: Job? = null

    init {
        viewModelScope.launch {
            backend.selection.collect { resetForBackendChange() }
        }
    }

    private fun resetForBackendChange() {
        val selection = backend.selection.value
        if (selection == draftBackend) return
        draftBackend = selection
        submissionJob?.cancel()
        submissionJob = null
        _uiState.value = FeedbackUiState(eventSink = eventSink)
    }

    private fun onEvent(event: FeedbackEvent) {
        resetForBackendChange()
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
        val selection = draftBackend
        submissionJob = viewModelScope.launch(ApiSubmissionBackend(selection)) {
            val result = sendFeedback(draft.message, draft.email)
            ensureActive()
            if (selection != backend.selection.value) return@launch
            _uiState.value = when (result) {
                is DataResult.Success -> FeedbackUiState(
                    submissionStatus = FeedbackSubmissionStatus.Sent,
                    eventSink = eventSink,
                )
                is DataResult.Failure -> draft.copy(submissionStatus = FeedbackSubmissionStatus.Failed(result.error))
            }
        }
    }
}
