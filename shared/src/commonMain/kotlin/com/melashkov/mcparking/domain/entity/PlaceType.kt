package com.melashkov.mcparking.domain.entity

enum class PlaceType(val value: String) {
    CITY("city"),
    TOWN("town"),
    VILLAGE("village"),
    HAMLET("hamlet"),
    SETTLEMENT("settlement"),
    SUBURB("suburb"),
    STREET("street"),
    POSTCODE("postcode"),
    STATION("station"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(value: String): PlaceType =
            entries.find { it.value == value } ?: UNKNOWN
    }
}
