package com.nukrs.windroid.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// 辅助数据类
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Serializable
data class FlashingTool(
    val id: String,
    val name: String,
    val description: String,
    @kotlinx.serialization.Transient
    val icon: ImageVector? = null,
    val category: String,
    val isEnabled: Boolean = true,
    val version: String = "1.0.0"
)

@Serializable
data class DeviceInfo(
    val model: String = "未连接",
    val manufacturer: String = "未知",
    val androidVersion: String = "未知",
    val buildNumber: String = "未知",
    val bootloaderStatus: String = "未知",
    val isRooted: Boolean = false,
    val serialNumber: String = "未知",
    val isConnected: Boolean = false,
    // 新增字段
    val uptime: String = "未知",
    val totalStorage: String = "未知",
    val availableStorage: String = "未知",
    val usedStorage: String = "未知",
    val storagePercentage: Int = 0,
    val installedApps: Int = 0,
    val securityPatch: String = "未知",
    val cpuInfo: String = "未知",
    val ramInfo: String = "未知",
    val batteryLevel: String = "未知"
)

@Serializable
data class FlashingTask(
    val id: String,
    val toolId: String,
    val deviceId: String,
    val status: TaskStatus,
    val progress: Float = 0f,
    val logs: List<String> = emptyList(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED
}

@Serializable
data class AppSettings(
    val adbPath: String = "",
    val fastbootPath: String = "",
    val odinPath: String = "",
    val spFlashToolPath: String = "",
    val autoDetectDevices: Boolean = true,
    val enableLogging: Boolean = true,
    val logLevel: String = "INFO",
    val theme: String = "SYSTEM"
)