package com.melashkov.mcparking.ui.shared

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.wrapped

internal fun Position.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = wrapped().longitude)
