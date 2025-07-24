package com.nukrs.windroid

import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.nukrs.windroid.ui.AppTray
import com.nukrs.windroid.ui.components.CloseConfirmDialog
import com.nukrs.windroid.ui.theme.WinDroidTheme
import com.nukrs.windroid.ui.screens.MainScreen

fun main() = application {
    var isWindowVisible by remember { mutableStateOf(true) }
    var showCloseDialog by remember { mutableStateOf(false) }
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    
    // 系统托盘
    AppTray(
        onShowWindow = {
            isWindowVisible = true
        },
        onExit = {
            exitApplication()
        }
    )
    
    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                showCloseDialog = true
            },
            title = "WinDroid Toolbox - Android刷机工具箱",
            icon = painterResource("tray_icon.svg"),
            state = windowState,
            visible = isWindowVisible
        ) {
            WinDroidTheme {
                MainScreen()
                
                // 关闭确认对话框
                if (showCloseDialog) {
                    CloseConfirmDialog(
                        onDismiss = {
                            showCloseDialog = false
                        },
                        onMinimizeToTray = {
                            showCloseDialog = false
                            isWindowVisible = false
                        },
                        onExit = {
                            exitApplication()
                        }
                    )
                }
            }
        }
    }
}