package com.melashkov.mcparking.data

import com.melashkov.mcparking.data.remote.dto.FeedbackRequestDto
import com.melashkov.mcparking.data.remote.dto.FeedbackResponseDto
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.FeedbackRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import org.koin.core.annotation.Singleton

@Singleton
class FeedbackRepositoryImpl(private val client: HttpClient) : FeedbackRepository {
    override suspend fun send(message: String, email: String): DataResult<Unit> = try {
        val response = client.post("feedback.php") {
            contentType(ContentType.Application.Json)
            setBody(FeedbackRequestDto(message, email))
        }.body<FeedbackResponseDto>()
        if (response.status == "OK") DataResult.Success(Unit)
        else DataResult.Failure(AppError.ServerUnavailable)
    } catch (error: CancellationException) {
        throw error
    } catch (_: IOException) {
        DataResult.Failure(AppError.ConnectionFailed)
    } catch (error: ClientRequestException) {
        DataResult.Failure(if (error.response.status.value == 429) AppError.RateLimited else AppError.Unknown)
    } catch (_: ServerResponseException) {
        DataResult.Failure(AppError.ServerUnavailable)
    } catch (_: Exception) {
        DataResult.Failure(AppError.Unknown)
    }
}
