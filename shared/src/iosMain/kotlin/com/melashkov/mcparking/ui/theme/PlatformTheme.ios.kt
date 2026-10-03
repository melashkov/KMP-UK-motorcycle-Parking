package com.melashkov.mcparking.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@Composable
internal actual fun ApplyPlatformTheme(dark: Boolean, followSystem: Boolean) {
    SideEffect {
        UIApplication.sharedApplication.keyWindow?.overrideUserInterfaceStyle =
            if (followSystem) UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            else if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }
}
