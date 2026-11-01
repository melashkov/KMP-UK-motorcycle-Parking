package com.melashkov.mcparking.ui.support

import com.melashkov.mcparking.domain.interfaces.AppError

sealed interface FeedbackEvent {
    data class MessageChanged(val value: String) : FeedbackEvent
    data class EmailChanged(val value: String) : FeedbackEvent
    data object Submit : FeedbackEvent
    data object Reset : FeedbackEvent
}

sealed interface FeedbackSubmissionStatus {
    data object Idle : FeedbackSubmissionStatus
    data object Sending : FeedbackSubmissionStatus
    data object Sent : FeedbackSubmissionStatus
    data class Failed(val error: AppError) : FeedbackSubmissionStatus
}

data class FeedbackUiState(
    val message: String = "",
    val email: String = "",
    val submissionStatus: FeedbackSubmissionStatus = FeedbackSubmissionStatus.Idle,
    val eventSink: (FeedbackEvent) -> Unit = {},
) {
    val validation: FeedbackValidation = FeedbackValidator.validate(message, email)
    val isSending: Boolean get() = submissionStatus == FeedbackSubmissionStatus.Sending
    val isSent: Boolean get() = submissionStatus == FeedbackSubmissionStatus.Sent
    val isEditable: Boolean get() = !isSending && !isSent
    val canSubmit: Boolean get() = isEditable && validation.valid
    val error: AppError? get() = (submissionStatus as? FeedbackSubmissionStatus.Failed)?.error
}
