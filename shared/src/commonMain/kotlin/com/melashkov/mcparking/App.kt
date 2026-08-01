package com.melashkov.mcparking

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.maplibre.compose.map.MaplibreMap

@Composable
@Preview
fun App() {
    MaterialTheme {
        MaplibreMap()
    }
}