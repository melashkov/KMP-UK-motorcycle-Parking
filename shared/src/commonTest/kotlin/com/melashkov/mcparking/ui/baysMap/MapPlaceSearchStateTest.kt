package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.entity.PlaceType
import com.melashkov.mcparking.testViewport
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

@OptIn(ExperimentalCoroutinesApi::class)
class MapPlaceSearchStateTest {
    private fun place(name: String) = Place(name, name, "England", PlaceType.CITY, 51.5, -0.5)

    @Test fun openingAndDismissingSearchPreservesDestination() = runTest {
        val state = MapPlaceSearchState(this, moveCamera = { null }, onEvent = {})
        assertFalse(state.isSearchVisible)
        assertNull(state.destination)
        state.openSearch()
        assertTrue(state.isSearchVisible)
        state.selectPlace(place("London"))
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
        val state = MapPlaceSearchState(this, moveCamera = {
            moves += it
            delay(100)
            viewport
        }, onEvent = events::add)
        val selected = place("London")

        repeat(2) {
            events.clear()
            state.openSearch()
            state.selectPlace(selected)
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
        val oldViewport = viewport.copy(zoom = 15.0)
        val state = MapPlaceSearchState(this, moveCamera = {
            if (it.name == "Oxford") withContext(NonCancellable) {
                delay(1_000)
                oldViewport
            } else viewport
        }, onEvent = events::add)

        state.selectPlace(place("Oxford"))
        runCurrent()
        state.selectPlace(place("London"))
        advanceUntilIdle()
        assertEquals(place("London"), state.destination)
        assertEquals(listOf(MapUiEvent.SearchDestinationArea(viewport)),
            events.filterIsInstance<MapUiEvent.SearchDestinationArea>())
    }

    @Test fun leavingMapCancelsMovementWithoutSearching() = runTest {
        val lifetime = Job(coroutineContext[Job])
        val scope = CoroutineScope(coroutineContext + lifetime)
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(scope, moveCamera = {
            delay(1_000)
            testViewport()
        }, onEvent = events::add)

        state.selectPlace(place("London"))
        runCurrent()
        scope.cancel()
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }

    @Test fun unavailableViewportDoesNotStartSearch() = runTest {
        val events = mutableListOf<MapUiEvent>()
        val state = MapPlaceSearchState(this, moveCamera = { null }, onEvent = events::add)
        state.selectPlace(place("London"))
        advanceUntilIdle()
        assertTrue(events.none { it is MapUiEvent.SearchDestinationArea })
    }
}
