package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.testViewport
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShouldShowSearchThisAreaUseCaseTest {
    private val useCase = ShouldShowSearchThisAreaUseCase()

    @Test
    fun hiddenBelowZoomThirteenEvenWithoutPreviousSearch() {
        assertFalse(
            useCase(
                viewport = testViewport(zoom = 12.99),
                lastSearchedViewport = null,
            ),
        )
    }

    @Test
    fun shownWhenNoAreaHasBeenSearched() {
        assertTrue(
            useCase(
                viewport = testViewport(zoom = 13.0),
                lastSearchedViewport = null,
            ),
        )
    }

    @Test
    fun hiddenWhenMovedViewportIsBelowMinimumZoom() {
        assertFalse(useCase(testViewport(zoom = 12.99), testViewport()))
    }

    @Test
    fun hiddenForUnchangedViewport() {
        val viewport = testViewport()

        assertFalse(useCase(viewport, viewport.copy()))
    }

    @Test
    fun shownAfterViewportMoves() {
        val previous = testViewport()
        val moved = previous.copy(
            center = GeoCoordinate(51.51, -0.12),
        )

        assertTrue(useCase(moved, previous))
    }
}
