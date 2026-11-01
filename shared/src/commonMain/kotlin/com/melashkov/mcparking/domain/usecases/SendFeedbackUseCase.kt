package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.FeedbackRepository
import org.koin.core.annotation.Singleton

@Singleton
class SendFeedbackUseCase(private val repository: FeedbackRepository) {
    suspend operator fun invoke(message: String, email: String): DataResult<Unit> =
        repository.send(message.trim(), email.trim())
}
