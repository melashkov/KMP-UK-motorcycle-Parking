package com.melashkov.mcparking.ui.bayEditor

import com.melashkov.mcparking.FakeParkingRepository
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.DataError
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
    fun emptyAddFormShowsRequiredErrorsWithoutSubmitting() {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        viewModel.initialize(addInitialData())

        viewModel.uiState.value.eventSink(BayEditorEvent.Submit)

        val state = viewModel.uiState.value
        assertEquals("Enter a name for this parking bay", state.titleError)
        assertEquals("Choose a parking type", state.typeError)
        assertTrue(repository.submissions.isEmpty())
    }

    @Test
    fun addFormDoesNotAcceptInactiveAsParkingType() {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        viewModel.initialize(addInitialData(title = "New bay"))

        viewModel.uiState.value.eventSink(
            BayEditorEvent.TypeChanged(ParkingType.INACTIVE),
        )
        viewModel.uiState.value.eventSink(BayEditorEvent.Submit)

        assertNull(viewModel.uiState.value.type)
        assertEquals("Choose a parking type", viewModel.uiState.value.typeError)
        assertTrue(repository.submissions.isEmpty())
    }

    @Test
    fun editFormCanSubmitInactiveReportWithParentId() = runTest(dispatcher) {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        viewModel.initialize(
            BayEditorInitialData(
                mode = BayEditorMode.Edit,
                parentId = "42",
                title = "  Station bays  ",
                description = "  Bays have been removed  ",
                type = ParkingType.FREE,
                location = TestLocation,
            ),
        )

        viewModel.uiState.value.eventSink(
            BayEditorEvent.TypeChanged(ParkingType.INACTIVE),
        )
        viewModel.uiState.value.eventSink(BayEditorEvent.Submit)
        advanceUntilIdle()

        val submission = repository.submissions.single()
        assertEquals("42", submission.parentId)
        assertEquals("Station bays", submission.title)
        assertEquals("Bays have been removed", submission.description)
        assertEquals(ParkingType.INACTIVE, submission.type)
        assertTrue(viewModel.uiState.value.isSubmitted)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun failedSubmissionShowsUsefulErrorAndCanBeRetried() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            submissionResult = DataResult.Failure(DataError.Offline),
        )
        val viewModel = createViewModel(repository)
        viewModel.initialize(
            addInitialData(
                title = "New bay",
                type = ParkingType.FREE,
            ),
        )

        viewModel.uiState.value.eventSink(BayEditorEvent.Submit)
        advanceUntilIdle()

        assertEquals(
            DataError.Offline,
            viewModel.uiState.value.submissionError,
        )
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isSubmitted)

        repository.submissionResult = DataResult.Success(Unit)
        viewModel.uiState.value.eventSink(BayEditorEvent.Submit)
        advanceUntilIdle()

        assertEquals(2, repository.submissions.size)
        assertTrue(viewModel.uiState.value.isSubmitted)
    }

    @Test
    fun locationPickerUpdatesLocationAndCloses() {
        val viewModel = createViewModel(FakeParkingRepository())
        viewModel.initialize(addInitialData())
        val newLocation = GeoCoordinate(51.50, -0.11)

        viewModel.uiState.value.eventSink(BayEditorEvent.ChooseLocation)
        assertTrue(viewModel.uiState.value.isLocationPickerOpen)

        viewModel.uiState.value.eventSink(BayEditorEvent.LocationSelected(newLocation))

        assertEquals(newLocation, viewModel.uiState.value.location)
        assertFalse(viewModel.uiState.value.isLocationPickerOpen)
    }

    @Test
    fun repeatedInitializationDoesNotDiscardDraftChanges() {
        val viewModel = createViewModel(FakeParkingRepository())
        viewModel.initialize(addInitialData(title = "Initial title"))
        viewModel.uiState.value.eventSink(BayEditorEvent.TitleChanged("Unsaved draft"))

        viewModel.initialize(addInitialData(title = "Reinitialized title"))

        assertEquals("Unsaved draft", viewModel.uiState.value.title)
    }

    private fun createViewModel(repository: FakeParkingRepository) =
        BayEditorViewModel(
            submitParkingBay = SubmitParkingBayUseCase(repository),
            appNavigationSink = AppNavigationSink(),
        )

    private fun addInitialData(
        title: String = "",
        type: ParkingType? = null,
    ) = BayEditorInitialData(
        mode = BayEditorMode.Add,
        title = title,
        type = type,
        location = TestLocation,
    )

    private companion object {
        val TestLocation = GeoCoordinate(51.5074, -0.1278)
    }
}
