package com.melashkov.mcparking

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onRestart() {
        super.onRestart()
        // Android 6's map GL surface can remain black after an external activity.
        // Recreate the window with saved Compose state and retained ViewModels.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
