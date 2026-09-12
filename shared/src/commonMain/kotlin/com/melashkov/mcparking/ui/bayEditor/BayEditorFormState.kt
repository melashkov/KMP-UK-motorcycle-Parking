package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType

enum class BayEditorMode {
    Add,
    Edit,
}

data class BayEditorInitialData(
    val mode: BayEditorMode,
    val parentId: String? = null,
    val title: String = "",
    val description: String = "",
    val type: ParkingType? = null,
    val location: GeoCoordinate,
)

internal enum class BayEditorFieldError {
    TitleRequired,
    TitleTooLong,
    DescriptionTooLong,
    TypeRequired,
    LocationRequired,
}

@Stable
internal class BayEditorFormState(
    val mode: BayEditorMode,
    private val parentId: String?,
    title: String,
    description: String,
    type: ParkingType?,
    location: GeoCoordinate?,
) {
    constructor(initialData: BayEditorInitialData) : this(
        mode = initialData.mode,
        parentId = initialData.parentId,
        title = initialData.title,
        description = initialData.description,
        type = initialData.type,
        location = initialData.location,
    )

    var title by mutableStateOf(title)
        private set

    var description by mutableStateOf(description)
        private set

    var type by mutableStateOf(type)
        private set

    var location by mutableStateOf(location)
        private set

    var titleError by mutableStateOf<BayEditorFieldError?>(null)
        private set

    var descriptionError by mutableStateOf<BayEditorFieldError?>(null)
        private set

    var typeError by mutableStateOf<BayEditorFieldError?>(null)
        private set

    var locationError by mutableStateOf<BayEditorFieldError?>(null)
        private set

    var isLocationPickerOpen by mutableStateOf(false)
        private set

    fun updateTitle(value: String) {
        if (value.length > TITLE_MAX_LENGTH) return
        title = value
        titleError = null
    }

    fun updateDescription(value: String) {
        if (value.length > DESCRIPTION_MAX_LENGTH) return
        description = value
        descriptionError = null
    }

    fun updateType(value: ParkingType) {
        if (!value.isAvailableFor(mode)) return
        type = value
        typeError = null
    }

    fun showLocationPicker() {
        isLocationPickerOpen = true
    }

    fun dismissLocationPicker() {
        isLocationPickerOpen = false
    }

    fun updateLocation(value: GeoCoordinate) {
        location = value
        locationError = null
        isLocationPickerOpen = false
    }

    fun submissionOrNull(): ParkingBaySubmission? {
        title = title.trim()
        description = description.trim()

        titleError = when {
            title.isEmpty() -> BayEditorFieldError.TitleRequired
            title.length > TITLE_MAX_LENGTH -> BayEditorFieldError.TitleTooLong
            else -> null
        }
        descriptionError =
            if (description.length > DESCRIPTION_MAX_LENGTH) {
                BayEditorFieldError.DescriptionTooLong
            } else {
                null
            }
        typeError = if (type?.isAvailableFor(mode) == true) {
            null
        } else {
            BayEditorFieldError.TypeRequired
        }
        locationError = if (location == null) {
            BayEditorFieldError.LocationRequired
        } else {
            null
        }

        if (
            titleError != null ||
            descriptionError != null ||
            typeError != null ||
            locationError != null
        ) {
            return null
        }

        return ParkingBaySubmission(
            parentId = parentId,
            title = title,
            description = description,
            type = checkNotNull(type),
            position = checkNotNull(location),
        )
    }

    internal fun savedValues(): List<Any> = listOf(
        mode.name,
        parentId != null,
        parentId.orEmpty(),
        title,
        description,
        type?.value ?: NoParkingType,
        location != null,
        location?.latitude ?: 0.0,
        location?.longitude ?: 0.0,
        titleError.savedName,
        descriptionError.savedName,
        typeError.savedName,
        locationError.savedName,
        isLocationPickerOpen,
    )

    companion object {
        const val TITLE_MAX_LENGTH = 60
        const val DESCRIPTION_MAX_LENGTH = 255

        val EditableParkingTypes = listOf(
            ParkingType.FREE,
            ParkingType.PAY,
            ParkingType.PERMIT,
            ParkingType.UNCATEGORISED,
        )

        internal fun restore(values: List<Any>): BayEditorFormState {
            val hasParentId = values[1] as Boolean
            val typeValue = (values[5] as Number).toInt()
            val hasLocation = values[6] as Boolean

            return BayEditorFormState(
                mode = BayEditorMode.valueOf(values[0] as String),
                parentId = (values[2] as String).takeIf { hasParentId },
                title = values[3] as String,
                description = values[4] as String,
                type = ParkingType.entries.find { it.value == typeValue },
                location = if (hasLocation) {
                    GeoCoordinate(
                        latitude = values[7] as Double,
                        longitude = values[8] as Double,
                    )
                } else {
                    null
                },
            ).apply {
                titleError = (values[9] as String).toFieldError()
                descriptionError = (values[10] as String).toFieldError()
                typeError = (values[11] as String).toFieldError()
                locationError = (values[12] as String).toFieldError()
                isLocationPickerOpen = values[13] as Boolean
            }
        }
    }
}

@Composable
internal fun rememberBayEditorFormState(
    initialData: BayEditorInitialData,
): BayEditorFormState = rememberSaveable(
    initialData,
    saver = listSaver(
        save = { it.savedValues() },
        restore = { BayEditorFormState.restore(it) },
    ),
) {
    BayEditorFormState(initialData)
}

private fun ParkingType.isAvailableFor(mode: BayEditorMode): Boolean =
    this in BayEditorFormState.EditableParkingTypes ||
        (mode == BayEditorMode.Edit && this == ParkingType.INACTIVE)

private val BayEditorFieldError?.savedName: String
    get() = this?.name.orEmpty()

private fun String.toFieldError(): BayEditorFieldError? =
    takeIf(String::isNotEmpty)?.let(BayEditorFieldError::valueOf)

private const val NoParkingType = -1
