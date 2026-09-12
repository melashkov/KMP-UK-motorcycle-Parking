package com.melashkov.mcparking

import androidx.compose.material3.MaterialTheme
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
    MaterialTheme {
        AppNavigation()
    }
}
