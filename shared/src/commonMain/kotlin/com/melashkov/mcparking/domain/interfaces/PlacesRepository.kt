package com.melashkov.mcparking.domain.interfaces

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place

interface PlacesRepository {
    suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>>
}
