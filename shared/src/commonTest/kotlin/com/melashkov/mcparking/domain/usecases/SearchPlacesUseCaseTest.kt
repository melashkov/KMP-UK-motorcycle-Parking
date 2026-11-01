package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.PlacesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchPlacesUseCaseTest {
    @Test fun shortQueriesDoNotReachRepository() = runTest {
        var calls = 0
        val search = searchPlaces { _, _ -> calls++; DataResult.Success(emptyList()) }
        for (query in listOf("", "   ", "Lo", "  Lo  ")) {
            assertEquals(SearchPlacesResult.QueryTooShort, search(query, null))
        }
        assertEquals(0, calls)
    }

    @Test fun trimsThenLimitsQueryAndPreservesLocationBias() = runTest {
        val requests = mutableListOf<String>()
        val near = GeoCoordinate(51.5, -0.1)
        val search = searchPlaces { query, location ->
            requests += query
            assertEquals(near, location)
            DataResult.Success(emptyList())
        }
        for (query in listOf("  Lon  ", "a".repeat(80), "  " + "b".repeat(81) + "  ")) {
            assertEquals(SearchPlacesResult.Success(emptyList()), search(query, near))
        }
        assertEquals(listOf("Lon", "a".repeat(80), "b".repeat(80)), requests)
    }

    @Test fun preservesRepositoryFailure() = runTest {
        val search = searchPlaces { _, _ -> DataResult.Failure(AppError.ConnectionFailed) }
        assertEquals(SearchPlacesResult.Failure(AppError.ConnectionFailed), search("London", null))
    }

    private fun searchPlaces(search: suspend (String, GeoCoordinate?) -> DataResult<List<Place>>) =
        SearchPlacesUseCase(object : PlacesRepository {
            override suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>> =
                search.invoke(query, near)
        })
}
