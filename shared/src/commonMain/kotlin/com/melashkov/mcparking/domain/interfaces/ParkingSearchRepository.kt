package com.melashkov.mcparking.domain.interfaces

import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay

interface ParkingSearchRepository {
    suspend fun getParkingBays(bounds: GeoBounds): DataResult<List<ParkingBay>>
}
