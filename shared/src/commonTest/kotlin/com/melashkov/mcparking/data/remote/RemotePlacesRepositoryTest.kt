package com.melashkov.mcparking.data.remote


import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.entity.PlaceType
import com.melashkov.mcparking.domain.interfaces.DataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemotePlacesRepositoryTest {
    @Test fun unknownApiTypeDoesNotDiscardSearchResults() = runTest {
        val engine = MockEngine {
            respond(
                """{"status":"OK","results":[
                    {"id":"1","name":"Leeds","subtitle":"England","type":"city","latitude":53.8,"longitude":-1.55},
                    {"id":"2","name":"New place","subtitle":"England","type":"future_type","latitude":51.5,"longitude":-0.1}
                ]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
            defaultRequest { url("https://parking.test/api/") }
        }
        try {
            val result = assertIs<DataResult.Success<List<Place>>>(RemotePlacesRepository(client).search("Leeds", null))
            assertEquals(listOf(PlaceType.CITY, PlaceType.UNKNOWN), result.value.map { it.type })
            assertEquals(listOf("Leeds", "New place"), result.value.map { it.name })
            assertEquals(53.8, result.value.first().latitude)
            assertEquals(-1.55, result.value.first().longitude)
        } finally {
            client.close()
        }
    }
}
