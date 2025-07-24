package com.nukrs.windroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nukrs.windroid.ui.theme.ThemeManager

/**
 * 主题切换按钮
 * 用于在日/夜主题之间切换
 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    
    IconButton(
        onClick = { ThemeManager.toggleTheme() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkTheme) "切换到浅色主题" else "切换到深色主题",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}