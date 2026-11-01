package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.PlacesRepository
import org.koin.core.annotation.Singleton

@Singleton
class SearchPlacesUseCase(private val repository: PlacesRepository) {
    suspend operator fun invoke(query: String, near: GeoCoordinate?): SearchPlacesResult {
        val normalizedQuery = query.trim().take(MAX_QUERY_LENGTH)
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return SearchPlacesResult.QueryTooShort

        return when (val result = repository.search(normalizedQuery, near)) {
            is DataResult.Success -> SearchPlacesResult.Success(result.value)
            is DataResult.Failure -> SearchPlacesResult.Failure(result.error)
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val MAX_QUERY_LENGTH = 80
    }
}

sealed interface SearchPlacesResult {
    data object QueryTooShort : SearchPlacesResult
    data class Success(val places: List<Place>) : SearchPlacesResult
    data class Failure(val error: AppError) : SearchPlacesResult
}
