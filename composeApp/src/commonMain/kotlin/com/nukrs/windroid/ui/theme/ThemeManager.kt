package com.nukrs.windroid.ui.theme

import androidx.compose.runtime.*

/**
 * 主题管理器
 * 用于管理应用的日/夜主题状态
 */
object ThemeManager {
    private var _isDarkTheme = mutableStateOf(true) // 默认为暗色主题
    val isDarkTheme: State<Boolean> = _isDarkTheme
    
    /**
     * 切换主题
     */
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    /**
     * 设置主题
     * @param isDark 是否为暗色主题
     */
    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}