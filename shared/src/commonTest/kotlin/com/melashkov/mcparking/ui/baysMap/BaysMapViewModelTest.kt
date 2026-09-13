package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.FakeParkingRepository
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.usecases.SearchParkingBaysUseCase
import com.melashkov.mcparking.domain.usecases.ShouldShowSearchThisAreaUseCase
import com.melashkov.mcparking.testBay
import com.melashkov.mcparking.testViewport
import com.melashkov.mcparking.ui.bayEditor.navigation.AddBayRoute
import com.melashkov.mcparking.ui.bayEditor.navigation.EditBayRoute
import com.melashkov.mcparking.ui.navigation.AppCommand
import com.melashkov.mcparking.ui.navigation.AppNavigationSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaysMapViewModelTest {
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
    fun initialViewportAutomaticallyLoadsBays() = runTest(dispatcher) {
        val bays = listOf(testBay(), testBay(id = "43", type = ParkingType.PAY))
        val repository = FakeParkingRepository(
            searchResult = DataResult.Success(bays),
        )
        val viewModel = createViewModel(repository)
        val viewport = testViewport()

        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(viewport))
        advanceUntilIdle()

        assertEquals(bays, viewModel.uiState.value.parkingBays)
        assertEquals(viewport, viewModel.uiState.value.currentViewport)
        assertEquals(listOf(viewport.bounds), repository.searchedBounds)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun movingMapShowsSearchButtonWithoutImmediatelySearching() = runTest(dispatcher) {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        val initial = testViewport()
        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(initial))
        advanceUntilIdle()
        val moved = testViewport(center = GeoCoordinate(51.51, -0.12))

        viewModel.uiState.value.eventSink(MapUiEvent.ViewportChanged(moved))

        assertTrue(viewModel.uiState.value.showSearchThisArea)
        assertEquals(1, repository.searchedBounds.size)
    }

    @Test
    fun searchButtonRemainsVisibleUntilMovedAreaIsSearched() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeParkingRepository())
        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(testViewport()))
        advanceUntilIdle()
        val moved = testViewport(center = GeoCoordinate(51.51, -0.12))

        viewModel.uiState.value.eventSink(MapUiEvent.ViewportChanged(moved))
        viewModel.uiState.value.eventSink(MapUiEvent.ViewportChanged(moved.copy()))

        assertTrue(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun movingDuringRequestKeepsSearchButtonForNewArea() = runTest(dispatcher) {
        val repository = FakeParkingRepository()
        val viewModel = createViewModel(repository)
        val initial = testViewport()
        val moved = testViewport(center = GeoCoordinate(51.51, -0.12))

        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(initial))
        viewModel.uiState.value.eventSink(MapUiEvent.ViewportChanged(moved))
        advanceUntilIdle()

        assertEquals(listOf(initial.bounds), repository.searchedBounds)
        assertEquals(moved, viewModel.uiState.value.currentViewport)
        assertTrue(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun failedSearchExposesDomainError() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            searchResult = DataResult.Failure(AppError.ConnectionFailed),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(testViewport()))
        advanceUntilIdle()

        assertEquals(AppError.ConnectionFailed, viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.showSearchThisArea)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun dismissingSearchErrorKeepsSearchActionAvailable() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            searchResult = DataResult.Failure(AppError.ServerUnavailable),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(testViewport()))
        advanceUntilIdle()
        viewModel.uiState.value.eventSink(MapUiEvent.DismissSearchMessage)

        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun movingMapClearsStaleSearchError() = runTest(dispatcher) {
        val repository = FakeParkingRepository(
            searchResult = DataResult.Failure(AppError.ConnectionFailed),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.value.eventSink(MapUiEvent.InitialViewport(testViewport()))
        advanceUntilIdle()
        viewModel.uiState.value.eventSink(
            MapUiEvent.ViewportChanged(
                testViewport(center = GeoCoordinate(51.51, -0.12)),
            ),
        )

        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun areaTooLargeIsExposedAsZoomGuidanceNotAnAppError() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeParkingRepository())

        viewModel.uiState.value.eventSink(
            MapUiEvent.InitialViewport(testViewport().copy(zoom = 12.0)),
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.requiresZoomToSearch)
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.showSearchThisArea)
    }

    @Test
    fun selectingAndDismissingBayUpdatesSheetState() {
        val viewModel = createViewModel(FakeParkingRepository())
        val bay = testBay()

        viewModel.uiState.value.eventSink(MapUiEvent.SelectedBay(bay))
        assertEquals(bay, viewModel.uiState.value.selectedBay)

        viewModel.uiState.value.eventSink(MapUiEvent.DismissBayDetails)
        assertNull(viewModel.uiState.value.selectedBay)
    }

    @Test
    fun addBayNavigationCarriesSelectedMapLocation() = runTest(dispatcher) {
        val navigation = AppNavigationSink()
        val viewModel = createViewModel(
            repository = FakeParkingRepository(),
            navigation = navigation,
        )
        val location = GeoCoordinate(51.50, -0.11)

        viewModel.uiState.value.eventSink(MapUiEvent.AddBay(location))

        val route = assertIs<AddBayRoute>(
            assertIs<AppCommand.Navigate>(navigation.commands.first()).route,
        )
        assertEquals(location.latitude, route.latitude)
        assertEquals(location.longitude, route.longitude)
    }

    @Test
    fun suggestEditNavigationCarriesCompleteBaySnapshot() = runTest(dispatcher) {
        val navigation = AppNavigationSink()
        val viewModel = createViewModel(
            repository = FakeParkingRepository(),
            navigation = navigation,
        )
        val bay = testBay(type = ParkingType.PERMIT)

        viewModel.uiState.value.eventSink(MapUiEvent.SuggestEdit(bay))

        val route = assertIs<EditBayRoute>(
            assertIs<AppCommand.Navigate>(navigation.commands.first()).route,
        )
        assertEquals(bay.id, route.bayId)
        assertEquals(bay.title, route.title)
        assertEquals(bay.description, route.description)
        assertEquals(bay.type.value, route.type)
        assertEquals(bay.position.latitude, route.latitude)
        assertEquals(bay.position.longitude, route.longitude)
    }

    private fun createViewModel(
        repository: FakeParkingRepository,
        navigation: AppNavigationSink = AppNavigationSink(),
    ) = BaysMapViewModel(
        searchParkingBays = SearchParkingBaysUseCase(repository),
        shouldShowSearchThisArea = ShouldShowSearchThisAreaUseCase(),
        appNavigationSink = navigation,
    )
}
