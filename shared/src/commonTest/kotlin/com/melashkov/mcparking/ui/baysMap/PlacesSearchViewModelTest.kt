package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.entity.PlaceType
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesSearchViewModelTest {
    @BeforeTest fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @AfterTest fun teardown() { Dispatchers.resetMain() }

    private fun place(name: String) = Place(name, name, "England", PlaceType.CITY, 51.75, -1.25)

    @Test fun onlyLatestQueryIsSentAfterDebounce() = runTest {
        val requests = mutableListOf<String>()
        val vm = PlacesSearchViewModel(object : PlacesRepository {
            override suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>> {
                requests += query
                return DataResult.Success(listOf(place(query)))
            }
        })
        vm.search("Lo", null)
        vm.search("Lon", null)
        advanceTimeBy(200)
        vm.search("London", null)
        advanceUntilIdle()
        assertEquals(listOf("London"), requests)
        assertEquals("London", vm.uiState.value.results.single().name)
        vm.reset()
        assertEquals(PlacesSearchState(), vm.uiState.value)
    }

    @Test fun lateCancelledResponseCannotReplaceNewResults() = runTest {
        val vm = PlacesSearchViewModel(object : PlacesRepository {
            override suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>> =
                withContext(NonCancellable) {
                    if (query == "Oxford") delay(1_000)
                    DataResult.Success(listOf(place(query)))
                }
        })
        vm.search("Oxford", null, immediate = true)
        runCurrent()
        vm.search("London", null, immediate = true)
        advanceUntilIdle()
        assertEquals("London", vm.uiState.value.results.single().name)
        assertFalse(vm.uiState.value.loading)
    }

    @Test fun closingSheetDiscardsPendingSearchAndLateResponse() = runTest {
        var requests = 0
        val vm = PlacesSearchViewModel(object : PlacesRepository {
            override suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>> =
                withContext(NonCancellable) {
                    requests++
                    delay(1_000)
                    DataResult.Success(listOf(place(query)))
                }
        })

        vm.search("London", null)
        vm.reset()
        advanceUntilIdle()
        assertEquals(0, requests)
        assertEquals(PlacesSearchState(), vm.uiState.value)

        vm.search("Oxford", null, immediate = true)
        runCurrent()
        assertEquals(1, requests)
        vm.reset()
        advanceUntilIdle()
        assertEquals(PlacesSearchState(), vm.uiState.value)
    }

    @Test fun destinationZoomKeepsParkingSearchLocalAndShowsSettlementOverview() {
        assertEquals(15.0, place("Street").copy(type = PlaceType.STREET).destinationZoom())
        assertEquals(15.0, place("Unknown").copy(type = PlaceType.UNKNOWN).destinationZoom())
        assertEquals(13.0, place("London").destinationZoom())
        assertEquals(13.0, place("Town").copy(type = PlaceType.TOWN).destinationZoom())
        assertEquals(13.0, place("Village").copy(type = PlaceType.VILLAGE).destinationZoom())
    }
}
