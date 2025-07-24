package com.nukrs.windroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope

@Composable
expect fun ApplicationScope.AppTray(
    onShowWindow: () -> Unit,
    onExit: () -> Unit
)