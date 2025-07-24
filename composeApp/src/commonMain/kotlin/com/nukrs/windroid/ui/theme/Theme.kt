package com.nukrs.windroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// Macchiato 主题配色
object MacchiatoColors {
    val rosewater = Color(0xFFF4DBD6)
    val flamingo = Color(0xFFF0C6C6)
    val pink = Color(0xFFF5BDE6)
    val mauve = Color(0xFFC6A0F6)
    val red = Color(0xFFED8796)
    val maroon = Color(0xFFEE99A0)
    val peach = Color(0xFFF5A97F)
    val yellow = Color(0xFFEED49F)
    val green = Color(0xFFA6DA95)
    val teal = Color(0xFF8BD5CA)
    val sky = Color(0xFF91D7E3)
    val sapphire = Color(0xFF7DC4E4)
    val blue = Color(0xFF8AADF4)
    val lavender = Color(0xFFB7BDF8)
    val text = Color(0xFFCAD3F5)
    val subtext1 = Color(0xFFB8C0E0)
    val subtext0 = Color(0xFFA5ADCB)
    val overlay2 = Color(0xFF939AB7)
    val overlay1 = Color(0xFF8087A2)
    val overlay0 = Color(0xFF6E738D)
    val surface2 = Color(0xFF5B6078)
    val surface1 = Color(0xFF494D64)
    val surface0 = Color(0xFF363A4F)
    val base = Color(0xFF24273A)
    val mantle = Color(0xFF1E2030)
    val crust = Color(0xFF181926)
}

// Macchiato 暗色主题
private val MacchiatoDarkColorScheme = darkColorScheme(
    primary = MacchiatoColors.blue,
    secondary = MacchiatoColors.mauve,
    tertiary = MacchiatoColors.pink,
    background = MacchiatoColors.base,
    surface = MacchiatoColors.surface0,
    surfaceVariant = MacchiatoColors.surface1,
    primaryContainer = MacchiatoColors.surface2,
    secondaryContainer = MacchiatoColors.surface1,
    tertiaryContainer = MacchiatoColors.surface0,
    error = MacchiatoColors.red,
    errorContainer = MacchiatoColors.surface0,
    onPrimary = MacchiatoColors.base, // 改为深色背景
    onSecondary = MacchiatoColors.base, // 改为深色背景
    onTertiary = MacchiatoColors.base, // 改为深色背景
    onBackground = MacchiatoColors.text,
    onSurface = MacchiatoColors.text,
    onSurfaceVariant = MacchiatoColors.subtext1,
    onPrimaryContainer = MacchiatoColors.text,
    onSecondaryContainer = MacchiatoColors.text,
    onTertiaryContainer = MacchiatoColors.text,
    onError = MacchiatoColors.base, // 改为深色背景
    onErrorContainer = MacchiatoColors.text,
    outline = MacchiatoColors.overlay0,
    outlineVariant = MacchiatoColors.surface2,
    scrim = MacchiatoColors.crust,
    inverseSurface = MacchiatoColors.surface2, // 改为深色表面
    inverseOnSurface = MacchiatoColors.text, // 保持浅色文字
    inversePrimary = MacchiatoColors.blue,
    surfaceTint = MacchiatoColors.blue
)

// Macchiato 浅色主题（优化对比度）
private val MacchiatoLightColorScheme = lightColorScheme(
    primary = Color(0xFF0066CC), // 更深的蓝色，提高对比度
    secondary = Color(0xFF7B4397), // 更深的紫色
    tertiary = Color(0xFFD63384), // 更深的粉色
    background = Color(0xFFFAFAFA), // 纯白背景
    surface = Color(0xFFFFFFFF), // 白色表面
    surfaceVariant = Color(0xFFF5F5F5), // 浅灰色变体
    primaryContainer = Color(0xFFE3F2FD), // 浅蓝色容器
    secondaryContainer = Color(0xFFF3E5F5), // 浅紫色容器
    tertiaryContainer = Color(0xFFFCE4EC), // 浅粉色容器
    error = Color(0xFFD32F2F), // 深红色错误
    errorContainer = Color(0xFFFFEBEE), // 浅红色错误容器
    onPrimary = Color(0xFFFFFFFF), // 主色上的白色文字
    onSecondary = Color(0xFFFFFFFF), // 次色上的白色文字
    onTertiary = Color(0xFFFFFFFF), // 第三色上的白色文字
    onBackground = Color(0xFF1A1A1A), // 背景上的深色文字
    onSurface = Color(0xFF1A1A1A), // 表面上的深色文字
    onSurfaceVariant = Color(0xFF424242), // 表面变体上的深灰色文字
    onPrimaryContainer = Color(0xFF0D47A1), // 主色容器上的深蓝色文字
    onSecondaryContainer = Color(0xFF4A148C), // 次色容器上的深紫色文字
    onTertiaryContainer = Color(0xFF880E4F), // 第三色容器上的深粉色文字
    onError = Color(0xFFFFFFFF), // 错误色上的白色文字
    onErrorContainer = Color(0xFFB71C1C), // 错误容器上的深红色文字
    outline = Color(0xFFBDBDBD), // 边框颜色
    outlineVariant = Color(0xFFE0E0E0), // 边框变体颜色
    scrim = Color(0xFF000000), // 遮罩颜色
    inverseSurface = Color(0xFF2E2E2E), // 反色表面
    inverseOnSurface = Color(0xFFF5F5F5), // 反色表面上的文字
    inversePrimary = MacchiatoColors.blue, // 反色主色
    surfaceTint = Color(0xFF0066CC) // 表面着色
)

@Composable
fun WinDroidTheme(
    content: @Composable () -> Unit
) {
    val isDarkTheme by ThemeManager.isDarkTheme
    
    val colorScheme = when {
        isDarkTheme -> MacchiatoDarkColorScheme
        else -> MacchiatoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}