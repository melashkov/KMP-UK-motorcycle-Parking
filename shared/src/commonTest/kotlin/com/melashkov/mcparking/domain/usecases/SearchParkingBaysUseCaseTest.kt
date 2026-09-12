package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.FakeParkingRepository
import com.melashkov.mcparking.TestBounds
import com.melashkov.mcparking.domain.interfaces.DataError
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
        val repository = FakeParkingRepository()
        val useCase = SearchParkingBaysUseCase(repository)

        val result = useCase(testViewport(zoom = 12.99))

        assertIs<SearchParkingResult.AreaTooLarge>(result)
        assertTrue(repository.searchedBounds.isEmpty())
    }

    @Test
    fun minimumZoomSearchesExactViewportBounds() = runTest {
        val bays = listOf(testBay())
        val repository = FakeParkingRepository(
            searchResult = DataResult.Success(bays),
        )
        val useCase = SearchParkingBaysUseCase(repository)

        val result = useCase(testViewport(zoom = 13.0))

        assertEquals(TestBounds, repository.searchedBounds.single())
        assertEquals(bays, assertIs<SearchParkingResult.Success>(result).bays)
    }

    @Test
    fun repositoryFailureIsPreserved() = runTest {
        val repository = FakeParkingRepository(
            searchResult = DataResult.Failure(DataError.Offline),
        )

        val result = SearchParkingBaysUseCase(repository)(testViewport())

        assertEquals(
            DataError.Offline,
            assertIs<SearchParkingResult.Failure>(result).error,
        )
    }
}
