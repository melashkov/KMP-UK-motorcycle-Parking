package com.melashkov.mcparking.data.remote.dto

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlin.test.Test
import kotlin.test.assertEquals

class ParkingDtoMappingTest {
    @Test
    fun locationDtoMapsEveryFieldToDomain() {
        val dto = LocationDto(
            id = 27,
            title = "Market Street",
            type = ParkingType.PAY.value,
            latitude = 51.51,
            longitude = -0.12,
            description = "Four spaces",
        )

        val bay = dto.toDomain()

        assertEquals("27", bay.id)
        assertEquals("Market Street", bay.title)
        assertEquals(ParkingType.PAY, bay.type)
        assertEquals(GeoCoordinate(51.51, -0.12), bay.position)
        assertEquals("Four spaces", bay.description)
    }

    @Test
    fun unknownApiTypeMapsToUnverified() {
        val bay = LocationDto(
            id = 1,
            title = "Unknown",
            type = 999,
            latitude = 51.51,
            longitude = -0.12,
            description = "",
        ).toDomain()

        assertEquals(ParkingType.UNVERIFIED, bay.type)
    }

    @Test
    fun submissionMapsToApiContract() {
        val dto = ParkingBaySubmission(
            parentId = "27",
            title = "Market Street",
            description = "Now permit only",
            type = ParkingType.PERMIT,
            position = GeoCoordinate(51.51, -0.12),
        ).toDto()

        assertEquals("Market Street", dto.title)
        assertEquals("Now permit only", dto.description)
        assertEquals(3, dto.type)
        assertEquals(51.51, dto.latitude)
        assertEquals(-0.12, dto.longitude)
        assertEquals("27", dto.parentId)
    }

    @Test
    fun newSubmissionMapsToApiContractWithoutParent() {
        val dto = ParkingBaySubmission(
            parentId = null,
            title = "New bay",
            description = "",
            type = ParkingType.FREE,
            position = GeoCoordinate(51.51, -0.12),
        ).toDto()

        assertEquals(null, dto.parentId)
    }
}
