package com.melashkov.mcparking

import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.MapViewport
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingRepository

internal class FakeParkingRepository(
    var searchResult: DataResult<List<ParkingBay>> = DataResult.Success(emptyList()),
    var submissionResult: DataResult<Unit> = DataResult.Success(Unit),
) : ParkingRepository {
    val searchedBounds = mutableListOf<GeoBounds>()
    val submissions = mutableListOf<ParkingBaySubmission>()

    override suspend fun getParkingBays(bounds: GeoBounds): DataResult<List<ParkingBay>> {
        searchedBounds += bounds
        return searchResult
    }

    override suspend fun submitParkingBay(
        submission: ParkingBaySubmission,
    ): DataResult<Unit> {
        submissions += submission
        return submissionResult
    }
}

internal fun testViewport(
    zoom: Double = 13.0,
    center: GeoCoordinate = GeoCoordinate(51.5074, -0.1278),
    bounds: GeoBounds = TestBounds,
) = MapViewport(
    bounds = bounds,
    zoom = zoom,
    center = center,
)

internal fun testBay(
    id: String = "42",
    title: String = "Station motorcycle parking",
    type: ParkingType = ParkingType.FREE,
    position: GeoCoordinate = GeoCoordinate(51.5074, -0.1278),
    description: String = "Six marked spaces",
) = ParkingBay(
    id = id,
    title = title,
    type = type,
    position = position,
    description = description,
)

internal val TestBounds = GeoBounds(
    north = 51.52,
    south = 51.49,
    east = -0.10,
    west = -0.15,
)
