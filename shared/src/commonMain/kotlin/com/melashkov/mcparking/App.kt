package com.melashkov.mcparking

import com.melashkov.mcparking.ui.theme.ParkingAppTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melashkov.mcparking.di.rememberKoinAppConfiguration
import com.melashkov.mcparking.ui.navigation.AppNavigation
import org.koin.compose.KoinApplication

@Preview
@Composable
fun App() {
    KoinApplication(
        configuration = rememberKoinAppConfiguration(),
    ) {
        AppContent()
    }
}

@Composable
fun AppContent() {
    ParkingAppTheme {
        AppNavigation()
    }
}
