package com.nukrs.windroid.core.service

import com.nukrs.windroid.data.model.DeviceInfo
import com.nukrs.windroid.data.model.Tuple4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class DeviceService {
    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _connectedDevices = MutableStateFlow<List<String>>(emptyList())
    val connectedDevices: StateFlow<List<String>> = _connectedDevices.asStateFlow()
    
    // Fastboot设备列表
    private val _fastbootDevices = MutableStateFlow<List<String>>(emptyList())
    val fastbootDevices: StateFlow<List<String>> = _fastbootDevices.asStateFlow()
    
    // ADB工具路径
    private val adbPath = getAdbPath()
    
    // Fastboot工具路径
    private val fastbootPath = getFastbootPath()
    
    private fun getFastbootPath(): String {
        // 首先尝试使用项目内置的Fastboot
        val resourcesPath = System.getProperty("compose.application.resources.dir") ?: ""
        val bundledFastboot = File(resourcesPath, "platform-tools/fastboot.exe")
        
        return if (bundledFastboot.exists()) {
            bundledFastboot.absolutePath
        } else {
            // 回退到系统PATH中的Fastboot
            "fastboot"
        }
    }
    
    private fun getAdbPath(): String {
        // 首先尝试使用项目内置的ADB
        val resourcesPath = System.getProperty("compose.application.resources.dir") ?: ""
        val bundledAdb = File(resourcesPath, "platform-tools/adb.exe")
        
        return if (bundledAdb.exists()) {
            bundledAdb.absolutePath
        } else {
            // 回退到系统PATH中的ADB
            "adb"
        }
    }
    
    suspend fun scanForDevices() {
        _isScanning.value = true
        try {
            val output = executeAdbCommand("devices")
            val devices = parseDevicesOutput(output)
            _connectedDevices.value = devices
            
            // 如果有设备连接，获取第一个设备的详细信息
            if (devices.isNotEmpty()) {
                val deviceInfo = getDeviceInfo(devices.first())
                if (deviceInfo != null) {
                    _deviceInfo.value = deviceInfo
                }
            } else {
                _deviceInfo.value = DeviceInfo()
            }
        } catch (e: Exception) {
            println("扫描设备时出错: ${e.message}")
            _connectedDevices.value = emptyList()
            _deviceInfo.value = DeviceInfo()
        } finally {
            _isScanning.value = false
        }
    }
    
    /**
     * 扫描Fastboot设备
     */
    suspend fun scanForFastbootDevices() {
        _isScanning.value = true
        try {
            val output = executeFastbootCommand("devices")
            val devices = parseFastbootDevicesOutput(output)
            _fastbootDevices.value = devices
            println("检测到Fastboot设备: $devices")
        } catch (e: Exception) {
            println("扫描Fastboot设备时出错: ${e.message}")
            _fastbootDevices.value = emptyList()
        } finally {
            _isScanning.value = false
        }
    }
    
    private fun parseFastbootDevicesOutput(output: String): List<String> {
        return output.lines()
            .filter { it.isNotBlank() && it.contains("\t") }
            .map { line ->
                line.split("\t")[0].trim()
            }
            .filter { it.isNotEmpty() }
    }
    
    private fun parseDevicesOutput(output: String): List<String> {
        return output.lines()
            .drop(1) // 跳过第一行 "List of devices attached"
            .filter { it.isNotBlank() && it.contains("\t") }
            .map { line ->
                line.split("\t")[0].trim()
            }
            .filter { it.isNotEmpty() }
    }
    
    private fun parseCpuBrandAndModel(rawCpuModel: String): Pair<String, String> {
        val model = rawCpuModel.lowercase()
        
        return when {
            // 高通
            model.contains("qualcomm") || 
            model.contains("snapdragon") || 
            model.contains("qcom") ||
            model.contains("msm") ||
            model.contains("sdm") ||
            model.contains("sm") ||
            model.contains("kona") ||
            model.contains("lahaina") ||
            model.contains("taro") ||
            model.contains("kalama") ||
            model.contains("pineapple") -> {
                val brand = "高通"
                val chipModel = when {
                    model.contains("snapdragon") -> {
                        // 提取骁龙型号
                        val snapdragonRegex = "snapdragon\\s*(\\d+)".toRegex()
                        val match = snapdragonRegex.find(model)
                        if (match != null) {
                            "骁龙 ${match.groupValues[1]}"
                        } else {
                            "骁龙处理器"
                        }
                    }
                    model.contains("msm") -> {
                        // MSM系列
                        val msmRegex = "msm(\\d+)".toRegex()
                        val match = msmRegex.find(model)
                        if (match != null) {
                            val msmNumber = match.groupValues[1]
                            val snapdragonModel = mapMsmToSnapdragon(msmNumber)
                            if (snapdragonModel.isNotEmpty()) {
                                "骁龙 $snapdragonModel"
                            } else {
                                "MSM$msmNumber"
                            }
                        } else {
                            "MSM处理器"
                        }
                    }
                    model.contains("sdm") -> {
                        // SDM系列
                        val sdmRegex = "sdm(\\d+)".toRegex()
                        val match = sdmRegex.find(model)
                        if (match != null) {
                            val sdmNumber = match.groupValues[1]
                            "骁龙 $sdmNumber"
                        } else {
                            "SDM处理器"
                        }
                    }
                    model.contains("sm") -> {
                        // SM系列
                        val smRegex = "sm(\\d+)".toRegex()
                        val match = smRegex.find(model)
                        if (match != null) {
                            val smNumber = match.groupValues[1]
                            val snapdragonModel = mapSmToSnapdragon(smNumber)
                            if (snapdragonModel.isNotEmpty()) {
                                "骁龙 $snapdragonModel"
                            } else {
                                "SM$smNumber"
                            }
                        } else {
                            "SM处理器"
                        }
                    }
                    // 代号识别
                    model.contains("sun") -> "骁龙 8 Elite Gen 1 (sun)"
                    model.contains("pineapple") -> "骁龙 8 Gen 3 (pineapple)"
                    model.contains("kona") -> "骁龙 865 (kona)"
                    model.contains("lahaina") -> "骁龙 888 (lahaina)"
                    model.contains("taro") -> "骁龙 8 Gen 1 (taro)"
                    model.contains("kalama") -> "骁龙 8 Gen 2 (kalama)"
                    model.contains("qcom") -> {
                        // 如果只有qcom，尝试获取更多信息
                        "高通处理器"
                    }
                    else -> "处理器"
                }
                Pair(brand, chipModel)
            }
            
            // 联发科处理器
            model.contains("mediatek") || model.contains("mtk") || model.contains("mt") -> {
                val brand = "联发科"
                val chipModel = when {
                    model.contains("dimensity") -> {
                        // 天玑系列
                        val dimensityRegex = "dimensity\\s*(\\d+)".toRegex()
                        val match = dimensityRegex.find(model)
                        if (match != null) {
                            "天玑 ${match.groupValues[1]}"
                        } else {
                            "天玑处理器"
                        }
                    }
                    model.contains("helio") -> {
                        // Helio系列
                        val helioRegex = "helio\\s*([a-z]\\d+)".toRegex()
                        val match = helioRegex.find(model)
                        if (match != null) {
                            "Helio ${match.groupValues[1].uppercase()}"
                        } else {
                            "Helio处理器"
                        }
                    }
                    model.contains("mt") -> {
                        // MT系列
                        val mtRegex = "mt(\\d+)".toRegex()
                        val match = mtRegex.find(model)
                        if (match != null) {
                            "MT${match.groupValues[1]}"
                        } else {
                            "MT处理器"
                        }
                    }
                    else -> "处理器"
                }
                Pair(brand, chipModel)
            }
            
            // 华为海思
            model.contains("hisilicon") || model.contains("kirin") -> {
                val brand = "华为海思"
                val chipModel = when {
                    model.contains("kirin") -> {
                        val kirinRegex = "kirin\\s*(\\d+)".toRegex()
                        val match = kirinRegex.find(model)
                        if (match != null) {
                            "麒麟 ${match.groupValues[1]}"
                        } else {
                            "麒麟处理器"
                        }
                    }
                    else -> "处理器"
                }
                Pair(brand, chipModel)
            }
            
            // 三星Exynos
            model.contains("exynos") -> {
                val brand = "三星"
                val exynosRegex = "exynos\\s*(\\d+)".toRegex()
                val match = exynosRegex.find(model)
                val chipModel = if (match != null) {
                    "Exynos ${match.groupValues[1]}"
                } else {
                    "Exynos处理器"
                }
                Pair(brand, chipModel)
            }
            
            // 紫光展锐
            model.contains("unisoc") || model.contains("spreadtrum") -> {
                val brand = "紫光展锐"
                val chipModel = when {
                    model.contains("tiger") -> "Tiger处理器"
                    model.contains("sc") -> {
                        val scRegex = "sc(\\d+)".toRegex()
                        val match = scRegex.find(model)
                        if (match != null) {
                            "SC${match.groupValues[1]}"
                        } else {
                            "SC处理器"
                        }
                    }
                    else -> "处理器"
                }
                Pair(brand, chipModel)
            }
            
            // 其他或未知
            else -> {
                // 尝试从原始字符串中提取有用信息
                val cleanModel = rawCpuModel.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim()
                if (cleanModel.isNotEmpty() && cleanModel != "未知处理器") {
                    Pair("未知品牌", cleanModel)
                } else {
                    Pair("未知品牌", "未知型号")
                }
            }
        }
    }
    
    // MSM编号到骁龙型号的映射
    private fun mapMsmToSnapdragon(msmNumber: String): String {
        return when (msmNumber) {
            "8998" -> "835"
            "8996" -> "820/821"
            "8994" -> "810"
            "8992" -> "808"
            "8974" -> "801"
            "8960" -> "S4 Pro"
            "8660" -> "S3"
            "8255" -> "S2"
            "7227" -> "S1"
            else -> ""
        }
    }
    
    // SM编号到骁龙型号的映射
    private fun mapSmToSnapdragon(smNumber: String): String {
        return when (smNumber) {
            "8750" -> "8 Elite Gen 1"
            "8650" -> "8 Gen 3"
            "8550" -> "8 Gen 2"
            "8475" -> "8+ Gen 1"
            "8450" -> "8 Gen 1"
            "8350" -> "888"
            "8250" -> "865"
            "8150" -> "855"
            "7325" -> "778G"
            "7225" -> "750G"
            "6375" -> "695"
            "6350" -> "690"
            else -> ""
        }
    }

    suspend fun getDeviceInfo(deviceId: String): DeviceInfo? {
        return try {
            val model = executeAdbCommand("-s $deviceId shell getprop ro.product.model").trim()
            val manufacturer = executeAdbCommand("-s $deviceId shell getprop ro.product.manufacturer").trim()
            val androidVersion = executeAdbCommand("-s $deviceId shell getprop ro.build.version.release").trim()
            val serialNumber = executeAdbCommand("-s $deviceId shell getprop ro.serialno").trim()
            val buildNumber = executeAdbCommand("-s $deviceId shell getprop ro.build.display.id").trim()
            val securityPatch = executeAdbCommand("-s $deviceId shell getprop ro.build.version.security_patch").trim()
            
            // 检查Bootloader状态
            val bootloaderStatus = try {
                val unlockStatus = executeAdbCommand("-s $deviceId shell getprop ro.boot.verifiedbootstate").trim()
                when (unlockStatus.lowercase()) {
                    "orange" -> "已解锁"
                    "yellow" -> "部分解锁"
                    "green" -> "已锁定"
                    else -> "未知"
                }
            } catch (e: Exception) {
                "未知"
            }
            
            // 检查Root状态
            val isRooted = try {
                val suResult = executeAdbCommand("-s $deviceId shell which su").trim()
                suResult.isNotEmpty() && !suResult.contains("not found")
            } catch (e: Exception) {
                false
            }
            
            // 获取开机时间
            val uptime = try {
                val uptimeSeconds = executeAdbCommand("-s $deviceId shell cat /proc/uptime").trim().split(" ")[0].toDouble()
                val hours = (uptimeSeconds / 3600).toInt()
                val minutes = ((uptimeSeconds % 3600) / 60).toInt()
                "${hours}小时${minutes}分钟"
            } catch (e: Exception) {
                "未知"
            }
            
            // 获取存储信息
            val (totalStorage, availableStorage, usedStorage, storagePercentage) = try {
                val dfOutput = executeAdbCommand("-s $deviceId shell df /data").trim()
                val lines = dfOutput.lines()
                if (lines.size >= 2) {
                    val dataLine = lines[1].split("\\s+".toRegex())
                    if (dataLine.size >= 4) {
                        val totalKb = dataLine[1].toLong()
                        val usedKb = dataLine[2].toLong()
                        val availableKb = dataLine[3].toLong()
                        
                        val totalGb = totalKb / 1024.0 / 1024.0
                        val usedGb = usedKb / 1024.0 / 1024.0
                        val availableGb = availableKb / 1024.0 / 1024.0
                        val percentage = (usedGb / totalGb * 100).toInt()
                        
                        val total = String.format("%.1f GB", totalGb)
                        val used = String.format("%.1f GB", usedGb)
                        val available = String.format("%.1f GB", availableGb)
                        
                        Tuple4(total, available, used, percentage)
                    } else {
                        Tuple4("未知", "未知", "未知", 0)
                    }
                } else {
                    Tuple4("未知", "未知", "未知", 0)
                }
            } catch (e: Exception) {
                Tuple4("未知", "未知", "未知", 0)
            }
            
            // 获取安装的应用数量
            val installedApps = try {
                val pmOutput = executeAdbCommand("-s $deviceId shell pm list packages").trim()
                pmOutput.lines().count { it.startsWith("package:") }
            } catch (e: Exception) {
                0
            }
            
            // 获取CPU信息
            val cpuInfo = try {
                val cpuInfoOutput = executeAdbCommand("-s $deviceId shell cat /proc/cpuinfo").trim()
                println("CPU信息原始输出: $cpuInfoOutput") // 调试信息
                val lines = cpuInfoOutput.lines()
                
                // 优先从系统属性获取平台代号信息
                val platformProp = executeAdbCommand("-s $deviceId shell getprop ro.board.platform").trim()
                val hardwareProp = executeAdbCommand("-s $deviceId shell getprop ro.hardware").trim()
                val chipsetProp = executeAdbCommand("-s $deviceId shell getprop ro.chipname").trim()
                
                println("硬件属性: $hardwareProp") // 调试信息
                println("平台属性: $platformProp") // 调试信息
                println("芯片属性: $chipsetProp") // 调试信息
                
                // 尝试获取CPU型号
                val modelLine = lines.find { 
                    it.startsWith("model name") || 
                    it.startsWith("Processor") || 
                    it.startsWith("Hardware") ||
                    it.startsWith("cpu model") ||
                    it.startsWith("Model")
                }
                
                // 优先使用平台代号，其次是硬件属性，最后是CPU信息
                val rawCpuModel = when {
                    platformProp.isNotEmpty() && platformProp != "unknown" -> platformProp
                    hardwareProp.isNotEmpty() && hardwareProp != "unknown" -> hardwareProp
                    chipsetProp.isNotEmpty() && chipsetProp != "unknown" -> chipsetProp
                    modelLine != null -> modelLine.substringAfter(":").trim()
                    else -> "未知处理器"
                }
                
                println("提取的CPU型号: $rawCpuModel") // 调试信息
                
                // 解析处理器品牌和型号
                val (brand, model) = parseCpuBrandAndModel(rawCpuModel)
                
                // 获取CPU核心数
                val coreCount = lines.count { it.startsWith("processor") }
                
                // 获取CPU频率
                val maxFreq = try {
                    val freqOutput = executeAdbCommand("-s $deviceId shell cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq").trim()
                    val freqMhz = freqOutput.toLongOrNull()?.div(1000) ?: 0
                    if (freqMhz > 0) {
                        val freqGhz = freqMhz / 1000.0
                        String.format("%.1f GHz", freqGhz)
                    } else {
                        ""
                    }
                } catch (e: Exception) {
                    ""
                }
                
                // 组合CPU信息
                buildString {
                    append("$brand $model")
                    if (coreCount > 0) {
                        append(" (${coreCount}核)")
                    }
                    if (maxFreq.isNotEmpty()) {
                        append(" @ $maxFreq")
                    }
                }
            } catch (e: Exception) {
                println("获取CPU信息失败: ${e.message}") // 调试信息
                "未知"
            }
            
            // 获取RAM信息
            val ramInfo = try {
                val memInfoOutput = executeAdbCommand("-s $deviceId shell cat /proc/meminfo").trim()
                val totalMemLine = memInfoOutput.lines().find { it.startsWith("MemTotal:") }
                if (totalMemLine != null) {
                    val totalKb = totalMemLine.split("\\s+".toRegex())[1].toLong()
                    val totalGb = totalKb / 1024 / 1024
                    "${totalGb}GB"
                } else {
                    "未知"
                }
            } catch (e: Exception) {
                "未知"
            }
            
            // 获取电池电量
            val batteryLevel = try {
                val batteryOutput = executeAdbCommand("-s $deviceId shell dumpsys battery").trim()
                val levelLine = batteryOutput.lines().find { it.contains("level:") }
                if (levelLine != null) {
                    val level = levelLine.substringAfter("level:").trim()
                    "$level%"
                } else {
                    "未知"
                }
            } catch (e: Exception) {
                "未知"
            }
            
            DeviceInfo(
                model = model.ifEmpty { "未知型号" },
                manufacturer = manufacturer.ifEmpty { "未知厂商" },
                androidVersion = androidVersion.ifEmpty { "未知版本" },
                serialNumber = serialNumber.ifEmpty { deviceId },
                buildNumber = buildNumber.ifEmpty { "未知" },
                bootloaderStatus = bootloaderStatus,
                isRooted = isRooted,
                isConnected = true,
                uptime = uptime,
                totalStorage = totalStorage,
                availableStorage = availableStorage,
                usedStorage = usedStorage,
                storagePercentage = storagePercentage,
                installedApps = installedApps,
                securityPatch = securityPatch.ifEmpty { "未知" },
                cpuInfo = cpuInfo,
                ramInfo = ramInfo,
                batteryLevel = batteryLevel
            )
        } catch (e: Exception) {
            println("获取设备信息时出错: ${e.message}")
            null
        }
    }
    
    suspend fun rebootToFastboot(deviceId: String): Boolean {
        return try {
            executeAdbCommand("-s $deviceId shell su -c 'reboot bootloader'")
            true
        } catch (e: Exception) {
            println("重启到Fastboot失败: ${e.message}")
            false
        }
    }
    
    suspend fun rebootToRecovery(deviceId: String): Boolean {
        return try {
            executeAdbCommand("-s $deviceId shell su -c 'reboot recovery'")
            true
        } catch (e: Exception) {
            println("重启到Recovery失败: ${e.message}")
            false
        }
    }
    
    suspend fun rebootToSystem(deviceId: String): Boolean {
        return try {
            executeAdbCommand("-s $deviceId shell su -c 'reboot'")
            true
        } catch (e: Exception) {
            println("重启到系统失败: ${e.message}")
            false
        }
    }
    
    /**
     * 执行自定义ADB命令
     * @param command ADB命令（不包含adb前缀）
     * @return 命令执行结果
     */
    suspend fun executeCustomCommand(command: String): String {
        return try {
            executeAdbCommand(command)
        } catch (e: Exception) {
            "错误: ${e.message}"
        }
    }
    
    /**
     * 执行自定义Fastboot命令
     * @param command Fastboot命令（不包含fastboot前缀）
     * @return 命令执行结果
     */
    suspend fun executeCustomFastbootCommand(command: String): String {
        return try {
            executeFastbootCommand(command)
        } catch (e: Exception) {
            "错误: ${e.message}"
        }
    }
    
    private suspend fun executeFastbootCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            val fullCommand = "$fastbootPath $command"
            println("执行Fastboot命令: $fullCommand")
            
            val process = ProcessBuilder()
                .command(fullCommand.split(" "))
                .redirectErrorStream(true)
                .start()
            
            val result = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor(30, TimeUnit.SECONDS)
            
            if (!exitCode) {
                process.destroyForcibly()
                throw RuntimeException("命令执行超时")
            }
            
            if (process.exitValue() != 0) {
                throw RuntimeException("命令执行失败: $result")
            }
            
            result
        } catch (e: Exception) {
            throw RuntimeException("执行Fastboot命令失败: ${e.message}", e)
        }
    }
    
    private suspend fun executeAdbCommand(command: String): String = withContext(Dispatchers.IO) {
        try {
            val fullCommand = "$adbPath $command"
            println("执行命令: $fullCommand")
            
            val process = ProcessBuilder()
                .command(fullCommand.split(" "))
                .redirectErrorStream(true)
                .start()
            
            val result = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor(30, TimeUnit.SECONDS)
            
            if (!exitCode) {
                process.destroyForcibly()
                throw RuntimeException("命令执行超时")
            }
            
            if (process.exitValue() != 0) {
                throw RuntimeException("命令执行失败: $result")
            }
            
            result
        } catch (e: Exception) {
            throw RuntimeException("执行ADB命令失败: ${e.message}", e)
        }
    }
}