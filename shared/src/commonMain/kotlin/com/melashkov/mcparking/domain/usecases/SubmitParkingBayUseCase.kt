package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingSubmissionRepository
import org.koin.core.annotation.Singleton

@Singleton
class SubmitParkingBayUseCase(
    private val repository: ParkingSubmissionRepository,
) {
    suspend operator fun invoke(submission: ParkingBaySubmission): DataResult<Unit> =
        repository.submitParkingBay(submission)
}
