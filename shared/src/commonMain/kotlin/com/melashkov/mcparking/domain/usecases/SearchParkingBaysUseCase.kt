package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.DataError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingRepository
import org.koin.core.annotation.Singleton

@Singleton
class SearchParkingBaysUseCase(
    private val repository: ParkingRepository,
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
        private const val MIN_SEARCH_ZOOM = 13.0
    }
}

sealed interface SearchParkingResult {

    data class Success(
        val bays: List<ParkingBay>,
    ) : SearchParkingResult

    data object AreaTooLarge : SearchParkingResult

    data class Failure(
        val error: DataError,
    ) : SearchParkingResult
}
