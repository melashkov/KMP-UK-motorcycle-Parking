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

        val bounds = viewport.bounds
        val searchAreas = if (bounds.east < bounds.west) {
            listOf(bounds.copy(east = 180.0), bounds.copy(west = -180.0))
        } else {
            listOf(bounds)
        }

        val bays = mutableListOf<ParkingBay>()
        for (area in searchAreas) {
            when (val result = repository.getParkingBays(area)) {
                is DataResult.Success -> bays.addAll(result.value)
                is DataResult.Failure -> return SearchParkingResult.Failure(result.error)
            }
        }
        return SearchParkingResult.Success(
            if (searchAreas.size == 1) bays else bays.distinctBy(ParkingBay::id),
        )
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
