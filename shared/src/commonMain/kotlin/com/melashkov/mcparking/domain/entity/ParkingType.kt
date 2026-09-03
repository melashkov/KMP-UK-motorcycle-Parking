package com.melashkov.mcparking.domain.entity

enum class ParkingType(val value: Int) {
    FREE(1),
    PAY(2),
    PERMIT(3),
    UNCATEGORISED(4),
    UNVERIFIED(5),
    INACTIVE(9);

    companion object {
        fun fromInt(value: Int): ParkingType =
            entries.find { it.value == value } ?: UNVERIFIED
    }
}
