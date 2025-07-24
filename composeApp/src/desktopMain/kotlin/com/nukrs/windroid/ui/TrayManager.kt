package com.nukrs.windroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray

@Composable
actual fun ApplicationScope.AppTray(
    onShowWindow: () -> Unit,
    onExit: () -> Unit
) {
    val icon: Painter = painterResource("tray_icon.svg")
    
    Tray(
        icon = icon,
        tooltip = "WinDroid Toolbox"
    ) {
        Item(
            text = "Show Window",
            onClick = onShowWindow
        )
        Separator()
        Item(
            text = "Exit",
            onClick = onExit
        )
    }
}