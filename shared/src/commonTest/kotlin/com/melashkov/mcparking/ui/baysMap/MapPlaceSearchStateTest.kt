package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.entity.PlaceType
import com.melashkov.mcparking.testViewport
import com.melashkov.mcparking.ui.shared.toGeoCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalCoroutinesApi::class)
class MapPlaceSearchStateTest {
    private fun place(name: String) = Place(name, name, "England", PlaceType.CITY, 51.5074, -0.1278)

    @Test fun openingAndDismissingSearchPreservesDestination() = runTest {
        val state = MapPlaceSearchState(this, onEvent = {})
        assertFalse(state.isSearchVisible)
        assertNull(state.destination)
        state.openSearch()
        assertTrue(state.isSearchVisible)
        state.selectPlace(place("London")) { null }
        assertFalse(state.isSearchVisible)
        state.openSearch()
        state.dismissSearch()
        assertFalse(state.isSearchVisible)
        assertEquals(place("London"), state.destination)
        advanceUntilIdle()
    }

    @Test fun selectionClosesSheetAndSearchesAfterMovingIncludingRepeatedSelections() = runTest {
        val viewport = testViewport()
        val events = mutableListOf<MapUiEvent>()
        val moves = mutableListOf<Place>()
        val state = MapPlaceSearchState(this, onEvent = events::add)
        val selected = place("London")

        repeat(2) {
            events.clear()
            state.openSearch()
            state.selectPlace(selected) {
                moves += it
                delay(100)
                viewport
            }
            assertFalse(state.isSearchVisible)
            assertEquals(selected, state.destination)
            assertEquals(listOf(MapUiEvent.CancelSearch, MapUiEvent.DismissBayDetails), events)
            advanceUntilIdle()
            assertEquals(MapUiEvent.SearchDestinationArea(viewport), events.last())
        }
        assertEquals(listOf(selected, selected), moves)
    }

    @Test fun replacedSelectionCannotSearchAfterALateAnimationCompletion() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val viewport = testViewport()
        val state = MapPlaceSearchState(this, onEvent = events::add)

        state.selectPlace(place("Oxford")) {
            withContext(NonCancellable) {
                delay(1_000)
                viewport
            }
        }
        runCurrent()
        state.selectPlace(place("London")) { viewport }
        advanceUntilIdle()
        assertEquals(place("London"), state.destination)
        assertEquals(listOf(MapUiEvent.SearchDestinationArea(viewport)),
            events.filterIsInstance<MapUiEvent.SearchDestinationArea>())
    }

    @Test fun leavingMapCancelsMovementWithoutSearching() = runTest {
        val lifetime = Job(coroutineContext[Job])
        val scope = CoroutineScope(coroutineContext + lifetime)
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(scope, onEvent = events::add)

        state.selectPlace(place("London")) {
            delay(1_000)
            testViewport()
        }
        runCurrent()
        scope.cancel()
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }

    @Test fun unavailableViewportDoesNotStartSearch() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(this, onEvent = events::add)
        state.selectPlace(place("London")) { null }
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }

    @Test fun supersededAnimationDoesNotSearchTheWrongViewport() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(this, onEvent = events::add)

        state.selectPlace(place("London")) {
            testViewport(center = GeoCoordinate(52.0, -0.5))
        }
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }

    @Test fun anotherCameraActionCancelsPendingSearch() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(this, onEvent = events::add)

        state.selectPlace(place("London")) {
            withContext(NonCancellable) {
                delay(1_000)
                testViewport()
            }
        }
        runCurrent()
        state.cancelPendingNavigation()
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }

    @Test fun destinationSearchAcceptsAnEquivalentWorldCopy() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(this, onEvent = events::add)
        val center = Position(540.0, 51.5).toGeoCoordinate()
        val viewport = testViewport(center = center)

        assertEquals(GeoCoordinate(51.5, -180.0), center)
        assertEquals(GeoCoordinate(51.5, 179.0), Position(-181.0, 51.5).toGeoCoordinate())
        state.selectPlace(place("Dateline").copy(latitude = 51.5, longitude = 180.0)) { viewport }
        advanceUntilIdle()
        assertEquals(MapUiEvent.SearchDestinationArea(viewport), events.last())
    }
}
