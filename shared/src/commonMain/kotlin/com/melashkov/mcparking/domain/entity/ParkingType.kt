package com.melashkov.mcparking.domain.entity

enum class ParkingType(val value: Int, val description: String) {
    FREE(1, "Free"),
    PAY(2, "Pay"),
    PERMIT(3, "Permit holders"),
    UNCATEGORISED(4, "Uncategorised"),
    UNVERIFIED(5, "Unverified"),
    INACTIVE(9, "Inactive");

    companion object {
        fun fromInt(value: Int): ParkingType =
            entries.find { it.value == value } ?: UNVERIFIED
    }
}
