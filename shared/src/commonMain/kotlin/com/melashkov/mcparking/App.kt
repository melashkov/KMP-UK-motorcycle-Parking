package com.melashkov.mcparking

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.melashkov.mcparking.di.KoinApp
import com.melashkov.mcparking.ui.baysMap.BaysMap
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration

@Preview
@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<KoinApp>()
    ) {
        AppContent()
    }
}

@Composable
fun AppContent() {
    MaterialTheme {
        BaysMap()
    }
}