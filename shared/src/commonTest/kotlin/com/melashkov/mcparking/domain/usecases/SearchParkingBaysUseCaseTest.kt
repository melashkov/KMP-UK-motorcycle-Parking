package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.FakeParkingSearchRepository
import com.melashkov.mcparking.TestBounds
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.testBay
import com.melashkov.mcparking.testViewport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SearchParkingBaysUseCaseTest {
    @Test
    fun belowMinimumZoomDoesNotCallRepository() = runTest {
        val repository = FakeParkingSearchRepository()
        val useCase = SearchParkingBaysUseCase(repository)

        val result = useCase(testViewport(zoom = 12.99))

        assertIs<SearchParkingResult.AreaTooLarge>(result)
        assertTrue(repository.searchedBounds.isEmpty())
    }

    @Test
    fun minimumZoomSearchesExactViewportBounds() = runTest {
        val bays = listOf(testBay())
        val repository = FakeParkingSearchRepository(
            searchResult = DataResult.Success(bays),
        )
        val useCase = SearchParkingBaysUseCase(repository)

        val result = useCase(testViewport(zoom = 13.0))

        assertEquals(TestBounds, repository.searchedBounds.single())
        assertEquals(bays, assertIs<SearchParkingResult.Success>(result).bays)
    }

    @Test
    fun antimeridianSearchCombinesBothSidesAndDeduplicatesBays() = runTest {
        val bounds = TestBounds.copy(west = 179.95, east = -179.99)
        val sharedBay = testBay(id = "shared")
        val westBay = testBay(id = "west")
        val eastBay = testBay(id = "east")
        val repository = FakeParkingSearchRepository(
            searchResult = DataResult.Success(listOf(westBay, sharedBay)),
        )
        repository.beforeSearch = {
            if (repository.searchedBounds.size == 2) {
                repository.searchResult = DataResult.Success(listOf(eastBay, sharedBay))
            }
        }

        val result = SearchParkingBaysUseCase(repository)(testViewport(bounds = bounds))

        assertEquals(
            listOf(bounds.copy(east = 180.0), bounds.copy(west = -180.0)),
            repository.searchedBounds,
        )
        assertEquals(listOf(westBay, sharedBay, eastBay), assertIs<SearchParkingResult.Success>(result).bays)
    }

    @Test
    fun failureOnSecondSideDoesNotReturnPartialResults() = runTest {
        val bounds = TestBounds.copy(west = 179.95, east = -179.99)
        val repository = FakeParkingSearchRepository(
            searchResult = DataResult.Success(listOf(testBay())),
        )
        repository.beforeSearch = {
            if (repository.searchedBounds.size == 2) {
                repository.searchResult = DataResult.Failure(AppError.ConnectionFailed)
            }
        }

        val result = SearchParkingBaysUseCase(repository)(testViewport(bounds = bounds))

        assertEquals(2, repository.searchedBounds.size)
        assertEquals(SearchParkingResult.Failure(AppError.ConnectionFailed), result)
    }

    @Test
    fun repositoryFailureIsPreserved() = runTest {
        val repository = FakeParkingSearchRepository(
            searchResult = DataResult.Failure(AppError.ConnectionFailed),
        )

        val result = SearchParkingBaysUseCase(repository)(testViewport())

        assertEquals(
            AppError.ConnectionFailed,
            assertIs<SearchParkingResult.Failure>(result).error,
        )
    }
}
