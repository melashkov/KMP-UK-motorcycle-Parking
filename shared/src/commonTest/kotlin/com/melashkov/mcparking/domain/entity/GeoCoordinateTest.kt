package com.melashkov.mcparking.domain.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeoCoordinateTest {
    @Test
    fun boundaryCoordinatesAreValid() {
        assertEquals(-90.0, GeoCoordinate(-90.0, -180.0).latitude)
        assertEquals(180.0, GeoCoordinate(90.0, 180.0).longitude)
    }

    @Test
    fun latitudeOutsideWorldBoundsIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            GeoCoordinate(latitude = 90.01, longitude = 0.0)
        }
    }

    @Test
    fun longitudeOutsideWorldBoundsIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            GeoCoordinate(latitude = 0.0, longitude = -180.01)
        }
    }
}
