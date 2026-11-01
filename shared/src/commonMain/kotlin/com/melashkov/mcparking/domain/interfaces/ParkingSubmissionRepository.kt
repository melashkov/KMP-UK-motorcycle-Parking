package com.melashkov.mcparking.domain.interfaces

import com.melashkov.mcparking.domain.entity.ParkingBaySubmission

interface ParkingSubmissionRepository {
    suspend fun submitParkingBay(submission: ParkingBaySubmission): DataResult<Unit>
}
