package com.melashkov.mcparking.ui.bayEditor

import com.melashkov.mcparking.FakeParkingRepository
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SubmitParkingBayUseCase
import com.melashkov.mcparking.ui.navigation.AppNavigationSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BayEditorViewModelTest {
    private lateinit var dispatcher: TestDispatcher

    @BeforeTest
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulSubmissionUsesTheDraftProvidedByTheScreen() = runTest(dispatcher) {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        val submission = validSubmission()

        viewModel.uiState.value.eventSink(BayEditorEvent.Submit(submission))
        advanceUntilIdle()

        assertEquals(submission, repository.submissions.single())
        assertTrue(viewModel.uiState.value.isSubmitted)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun failedSubmissionShowsUsefulErrorAndCanBeRetried() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            submissionResult = DataResult.Failure(AppError.ConnectionFailed),
        )
        val viewModel = createViewModel(repository)
        val submission = validSubmission()

        viewModel.uiState.value.eventSink(BayEditorEvent.Submit(submission))
        advanceUntilIdle()

        assertEquals(AppError.ConnectionFailed, viewModel.uiState.value.submissionError)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isSubmitted)

        repository.submissionResult = DataResult.Success(Unit)
        viewModel.uiState.value.eventSink(BayEditorEvent.Submit(submission))
        advanceUntilIdle()

        assertEquals(2, repository.submissions.size)
        assertTrue(viewModel.uiState.value.isSubmitted)
    }

    @Test
    fun changingDraftClearsPreviousSubmissionError() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            submissionResult = DataResult.Failure(AppError.ConnectionFailed),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.value.eventSink(BayEditorEvent.Submit(validSubmission()))
        advanceUntilIdle()
        viewModel.uiState.value.eventSink(BayEditorEvent.ClearSubmissionError)

        assertNull(viewModel.uiState.value.submissionError)
    }

    private fun createViewModel(repository: FakeParkingRepository) =
        BayEditorViewModel(
            submitParkingBay = SubmitParkingBayUseCase(repository),
            appNavigationSink = AppNavigationSink(),
        )

    private fun validSubmission() = ParkingBaySubmission(
        parentId = "42",
        title = "Station bays",
        description = "Six marked spaces",
        type = ParkingType.FREE,
        position = TestLocation,
    )

    private companion object {
        val TestLocation = GeoCoordinate(51.5074, -0.1278)
    }
}
