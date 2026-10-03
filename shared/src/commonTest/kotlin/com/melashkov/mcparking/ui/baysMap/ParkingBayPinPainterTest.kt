package com.melashkov.mcparking.ui.baysMap

import androidx.compose.ui.graphics.Color
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlin.test.Test
import kotlin.test.assertEquals

class ParkingBayPinPainterTest {

    @Test
    fun parkingTypesUseExpectedPinColors() {
        assertEquals(Color(0xFF357BC0), ParkingType.FREE.parkingBayColor)
        assertEquals(Color(0xFFA94F4F), ParkingType.PAY.parkingBayColor)
        assertEquals(Color(0xFF388E3C), ParkingType.PERMIT.parkingBayColor)
        assertEquals(Color(0xFFA65D00), ParkingType.UNCATEGORISED.parkingBayColor)
        assertEquals(Color(0xFF30343B), ParkingType.UNVERIFIED.parkingBayColor)
        assertEquals(Color(0xFF30343B), ParkingType.INACTIVE.parkingBayColor)
    }
}
