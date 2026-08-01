package com.melashkov.mcparking

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.melashkov.mcparking.parkingBays.ParkingBaysMap

@Composable
@Preview
fun App() {
    MaterialTheme {
        ParkingBaysMap()
    }
}