package com.melashkov.mcparking.ui.support

import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.FeedbackRepository
import com.melashkov.mcparking.domain.usecases.SendFeedbackUseCase
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {
    @BeforeTest fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @AfterTest fun teardown() { Dispatchers.resetMain() }

    @Test fun failurePreservesDraftAndRetryCannotDoubleSubmit() = runTest {
        var calls = 0
        val vm = viewModel { message, email ->
            calls++
            assertEquals("A useful idea", message)
            assertEquals("me@example.org", email)
            delay(100)
            if (calls == 1) DataResult.Failure(AppError.ServerUnavailable) else DataResult.Success(Unit)
        }
        vm.event(FeedbackEvent.MessageChanged(" A useful idea "))
        vm.event(FeedbackEvent.EmailChanged(" me@example.org "))
        vm.event(FeedbackEvent.Submit)
        assertTrue(vm.uiState.value.isSending)
        assertFalse(vm.uiState.value.isEditable)
        assertFalse(vm.uiState.value.canSubmit)
        vm.event(FeedbackEvent.Submit)
        vm.event(FeedbackEvent.Reset)
        vm.event(FeedbackEvent.MessageChanged("Cannot edit while sending"))
        vm.event(FeedbackEvent.EmailChanged("other@example.org"))
        advanceUntilIdle()
        assertEquals(1, calls)
        assertEquals(" A useful idea ", vm.uiState.value.message)
        assertEquals(" me@example.org ", vm.uiState.value.email)
        assertEquals(AppError.ServerUnavailable, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSending)
        assertTrue(vm.uiState.value.canSubmit)
        vm.event(FeedbackEvent.Submit)
        assertNull(vm.uiState.value.error)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSent)
        assertEquals("", vm.uiState.value.message)
        assertEquals("", vm.uiState.value.email)
        vm.event(FeedbackEvent.Submit)
        vm.event(FeedbackEvent.MessageChanged("Cannot edit after success"))
        vm.event(FeedbackEvent.EmailChanged("other@example.org"))
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals("", vm.uiState.value.message)
        assertEquals("", vm.uiState.value.email)
        vm.event(FeedbackEvent.Reset)
        assertEquals(FeedbackSubmissionStatus.Idle, vm.uiState.value.submissionStatus)
        assertFalse(vm.uiState.value.canSubmit)
        assertTrue(vm.uiState.value.isEditable)
        vm.event(FeedbackEvent.MessageChanged("Another useful idea"))
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test fun editingEitherFieldClearsSubmissionError() = runTest {
        val vm = viewModel { _, _ -> DataResult.Failure(AppError.ConnectionFailed) }
        vm.event(FeedbackEvent.MessageChanged("A useful idea"))
        for (edit in listOf(
            FeedbackEvent.MessageChanged("Another useful idea"),
            FeedbackEvent.EmailChanged("me@example.org"),
        )) {
            vm.event(FeedbackEvent.Submit)
            advanceUntilIdle()
            assertEquals(AppError.ConnectionFailed, vm.uiState.value.error)
            vm.event(edit)
            assertNull(vm.uiState.value.error)
            assertEquals(FeedbackSubmissionStatus.Idle, vm.uiState.value.submissionStatus)
        }
        vm.event(FeedbackEvent.Reset)
        assertEquals("", vm.uiState.value.message)
        assertEquals("", vm.uiState.value.email)
    }

    @Test fun invalidDraftDoesNotReachRepository() = runTest {
        var calls = 0
        val vm = viewModel { _, _ -> calls++; DataResult.Success(Unit) }
        vm.event(FeedbackEvent.Submit)
        vm.event(FeedbackEvent.MessageChanged("test"))
        vm.event(FeedbackEvent.Submit)
        vm.event(FeedbackEvent.MessageChanged("A useful idea"))
        vm.event(FeedbackEvent.EmailChanged("invalid"))
        vm.event(FeedbackEvent.Submit)
        advanceUntilIdle()
        assertEquals(0, calls)
        assertEquals(FeedbackSubmissionStatus.Idle, vm.uiState.value.submissionStatus)
    }

    private fun viewModel(send: suspend (String, String) -> DataResult<Unit>) =
        FeedbackViewModel(SendFeedbackUseCase(object : FeedbackRepository {
            override suspend fun send(message: String, email: String): DataResult<Unit> = send.invoke(message, email)
        }))

    private fun FeedbackViewModel.event(event: FeedbackEvent) = uiState.value.eventSink(event)
}
