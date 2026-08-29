package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.ParkingBay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal const val PARKING_BAY_ID = "parking_bay_id"
internal const val PARKING_BAY_FEATURE_ID = "parking_bay_feature_id"
internal const val PARKING_BAY_TYPE = "parking_bay_type"

internal fun List<ParkingBay>.toFeatureCollection(): FeatureCollection<Point, JsonObject> =
    FeatureCollection(
        map { bay ->
            Feature(
                id = JsonPrimitive(bay.id),
                geometry = Point(Position(bay.position.longitude, bay.position.latitude)),
                properties = buildJsonObject {
                    put(PARKING_BAY_FEATURE_ID, bay.id)
                    put(PARKING_BAY_ID, bay.id)
                    put(PARKING_BAY_TYPE, bay.type.value)
                    put("title", bay.title)
                },
            )
        }
    )
