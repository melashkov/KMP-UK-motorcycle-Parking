package com.melashkov.mcparking.domain.interfaces

import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission

interface ParkingRepository {
    suspend fun getParkingBays(
        bounds: GeoBounds,
    ): DataResult<List<ParkingBay>>

    suspend fun submitParkingBay(
        submission: ParkingBaySubmission,
    ): DataResult<Unit>
}
