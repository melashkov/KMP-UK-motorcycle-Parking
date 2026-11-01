package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingSearchRepository
import org.koin.core.annotation.Singleton

@Singleton
class SearchParkingBaysUseCase(
    private val repository: ParkingSearchRepository,
) {
    suspend operator fun invoke(
        viewport: MapViewport,
    ): SearchParkingResult {

        if (viewport.zoom < MIN_SEARCH_ZOOM) {
            return SearchParkingResult.AreaTooLarge
        }

        return when (
            val result = repository.getParkingBays(viewport.bounds)
        ) {
            is DataResult.Success ->
                SearchParkingResult.Success(result.value)

            is DataResult.Failure ->
                SearchParkingResult.Failure(result.error)
        }
    }

    companion object {
        internal const val MIN_SEARCH_ZOOM = 13.0
    }
}

sealed interface SearchParkingResult {

    data class Success(
        val bays: List<ParkingBay>,
    ) : SearchParkingResult

    data object AreaTooLarge : SearchParkingResult

    data class Failure(
        val error: AppError,
    ) : SearchParkingResult
}
