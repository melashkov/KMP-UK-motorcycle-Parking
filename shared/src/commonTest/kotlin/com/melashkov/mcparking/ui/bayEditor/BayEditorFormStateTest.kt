package com.melashkov.mcparking.ui.bayEditor

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BayEditorFormStateTest {
    @Test
    fun emptyAddFormShowsRequiredErrors() {
        val state = BayEditorFormState(addInitialData())

        assertNull(state.submissionOrNull())

        assertEquals(BayEditorFieldError.TitleRequired, state.titleError)
        assertEquals(BayEditorFieldError.TypeRequired, state.typeError)
    }

    @Test
    fun addFormDoesNotAcceptInactiveAsParkingType() {
        val state = BayEditorFormState(addInitialData(title = "New bay"))

        state.updateType(ParkingType.INACTIVE)

        assertNull(state.type)
        assertNull(state.submissionOrNull())
        assertEquals(BayEditorFieldError.TypeRequired, state.typeError)
    }

    @Test
    fun editFormCreatesInactiveSubmissionWithParentId() {
        val state = BayEditorFormState(
            BayEditorInitialData(
                mode = BayEditorMode.Edit,
                parentId = "42",
                title = "  Station bays  ",
                description = "  Bays have been removed  ",
                type = ParkingType.FREE,
                location = TestLocation,
            ),
        )

        state.updateType(ParkingType.INACTIVE)
        val submission = state.submissionOrNull()

        assertEquals("42", submission?.parentId)
        assertEquals("Station bays", submission?.title)
        assertEquals("Bays have been removed", submission?.description)
        assertEquals(ParkingType.INACTIVE, submission?.type)
    }

    @Test
    fun locationPickerUpdatesLocationAndCloses() {
        val state = BayEditorFormState(addInitialData())
        val newLocation = GeoCoordinate(51.50, -0.11)

        state.showLocationPicker()
        assertTrue(state.isLocationPickerOpen)

        state.updateLocation(newLocation)

        assertEquals(newLocation, state.location)
        assertFalse(state.isLocationPickerOpen)
    }

    @Test
    fun saveAndRestorePreservesDraftAndPickerState() {
        val state = BayEditorFormState(
            BayEditorInitialData(
                mode = BayEditorMode.Edit,
                parentId = "42",
                title = "Initial title",
                type = ParkingType.FREE,
                location = TestLocation,
            ),
        )
        state.updateTitle("Unsaved title")
        state.updateDescription("Unsaved description")
        state.updateType(ParkingType.INACTIVE)
        state.showLocationPicker()

        val restored = BayEditorFormState.restore(state.savedValues())
        val submission = restored.submissionOrNull()

        assertEquals(BayEditorMode.Edit, restored.mode)
        assertEquals("Unsaved title", restored.title)
        assertEquals("Unsaved description", restored.description)
        assertEquals(ParkingType.INACTIVE, restored.type)
        assertEquals(TestLocation, restored.location)
        assertTrue(restored.isLocationPickerOpen)
        assertEquals("42", submission?.parentId)
    }

    private fun addInitialData(
        title: String = "",
        type: ParkingType? = null,
    ) = BayEditorInitialData(
        mode = BayEditorMode.Add,
        title = title,
        type = type,
        location = TestLocation,
    )

    private companion object {
        val TestLocation = GeoCoordinate(51.5074, -0.1278)
    }
}
