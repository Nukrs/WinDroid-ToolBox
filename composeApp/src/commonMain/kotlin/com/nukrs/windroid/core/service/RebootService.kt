package com.nukrs.windroid.core.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 重启服务 - 处理设备的高级重启选项
 */
class RebootService {
    
    enum class RebootMode(val command: String, val displayName: String, val description: String) {
        NORMAL("reboot", "正常重启", "重启到系统"),
        RECOVERY("reboot recovery", "Recovery模式", "重启到Recovery恢复模式"),
        BOOTLOADER("reboot bootloader", "Bootloader模式", "重启到Fastboot/Download模式"),
        FASTBOOT("reboot fastboot", "Fastboot模式", "重启到Fastboot模式"),
        DOWNLOAD("reboot download", "Download模式", "重启到Download模式"),
        SAFE_MODE("reboot safemode", "安全模式", "重启到安全模式"),
        POWER_OFF("reboot -p", "关机", "关闭设备电源")
    }
    
    /**
     * 检查设备是否已Root
     */
    suspend fun checkRootStatus(deviceId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder("adb", "-s", deviceId, "shell", "su", "-c", "id")
                    .redirectErrorStream(true)
                    .start()
                
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()
                reader.close()
                
                val exitCode = process.waitFor()
                
                // 如果命令成功执行且输出包含uid=0，说明有root权限
                exitCode == 0 && output.contains("uid=0")
            } catch (e: Exception) {
                println("检查Root状态失败: ${e.message}")
                false
            }
        }
    }
    
    /**
     * 执行重启命令
     */
    suspend fun executeReboot(deviceId: String, mode: RebootMode): RebootResult {
        return withContext(Dispatchers.IO) {
            try {
                // 所有重启命令都使用su权限执行
                val command = listOf("adb", "-s", deviceId, "shell", "su", "-c", mode.command)
                
                println("执行重启命令: ${command.joinToString(" ")}")
                
                val process = ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
                
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()
                reader.close()
                
                val exitCode = process.waitFor()
                
                if (exitCode == 0) {
                    RebootResult.Success(mode, "重启命令执行成功")
                } else {
                    val errorMsg = if (output.contains("not found") || output.contains("su:")) {
                        "需要Root权限才能执行此操作"
                    } else {
                        "重启命令执行失败: $output"
                    }
                    RebootResult.Error(mode, errorMsg)
                }
            } catch (e: Exception) {
                println("执行重启命令失败: ${e.message}")
                RebootResult.Error(mode, "执行重启命令时发生错误: ${e.message}")
            }
        }
    }
    
    /**
     * 获取可用的重启选项（根据设备状态）
     */
    suspend fun getAvailableRebootModes(deviceId: String): List<RebootMode> {
        val isRooted = checkRootStatus(deviceId)
        
        return if (isRooted) {
            // Root设备可以使用所有重启选项
            RebootMode.values().toList()
        } else {
            // 非Root设备无法使用任何重启选项（因为现在都需要su权限）
            emptyList()
        }
    }
}

/**
 * 重启操作结果
 */
sealed class RebootResult {
    data class Success(val mode: RebootService.RebootMode, val message: String) : RebootResult()
    data class Error(val mode: RebootService.RebootMode, val error: String) : RebootResult()
}