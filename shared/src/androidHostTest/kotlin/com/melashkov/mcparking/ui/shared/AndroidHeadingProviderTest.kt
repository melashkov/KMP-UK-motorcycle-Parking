package com.melashkov.mcparking.ui.shared

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.compose.location.AndroidHeadingProvider
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AndroidHeadingProviderTest {
    @Test
    fun missingRotationVectorSensorCompletesWithoutCrashing() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).forEach {
            shadowOf(sensorManager).removeSensor(it)
        }
        assertNull(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))

        // Regression: MapLibre 0.13.1 threw here on the Galaxy A10s / Android 11.
        val headings = AndroidHeadingProvider(context).updates().toList()

        assertEquals(emptyList(), headings)
    }
}
