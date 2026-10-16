package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
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

    private fun place(name: String) = Place(name, name, "England", "city", 51.75, -1.25)

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

    @Test fun destinationZoomKeepsParkingSearchLocalAndShowsSettlementOverview() {
        assertEquals(15.0, place("Street").copy(type = "street").destinationZoom())
        assertEquals(13.0, place("London").destinationZoom())
        assertEquals(13.0, place("Town").copy(type = "town").destinationZoom())
        assertEquals(13.0, place("Village").copy(type = "village").destinationZoom())
    }
}
