package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.testBay
import com.melashkov.mcparking.ui.navigation.ExternalAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParkingBayActionsTest {
    @Test
    fun directionsUsesBayLocationAndTitle() {
        val action = assertIs<ExternalAction.Directions>(
            testBay(title = "Waterloo Road").directionsAction(),
        )

        assertEquals(51.5074, action.latitude)
        assertEquals(-0.1278, action.longitude)
        assertEquals("Waterloo Road", action.label)
    }

    @Test
    fun blankTitleUsesFallbackForDirectionsAndSharing() {
        val bay = testBay(title = "   ")

        assertEquals(
            "Motorcycle parking",
            assertIs<ExternalAction.Directions>(bay.directionsAction()).label,
        )
        assertEquals(
            "Motorcycle parking\n" +
                "https://www.google.com/maps/search/?api=1&query=51.5074,-0.1278",
            assertIs<ExternalAction.ShareText>(bay.shareAction()).text,
        )
    }

    @Test
    fun streetViewUsesExactBayCoordinates() {
        assertEquals(
            "https://www.google.com/maps/@?api=1" +
                "&map_action=pano&viewpoint=51.5074,-0.1278",
            assertIs<ExternalAction.OpenUri>(testBay().streetViewAction()).uri,
        )
    }
}
