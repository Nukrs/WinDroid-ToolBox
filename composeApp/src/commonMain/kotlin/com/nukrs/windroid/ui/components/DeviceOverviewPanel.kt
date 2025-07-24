package com.nukrs.windroid.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nukrs.windroid.data.model.DeviceInfo
import com.nukrs.windroid.core.service.DeviceService
import com.nukrs.windroid.core.service.RebootService
import com.nukrs.windroid.ui.theme.MacchiatoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun DeviceOverviewPanel(
    modifier: Modifier = Modifier,
    deviceService: DeviceService = remember { DeviceService() }
) {
    val deviceInfo by deviceService.deviceInfo.collectAsState()
    val connectedDevices by deviceService.connectedDevices.collectAsState()
    val isScanning by deviceService.isScanning.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // 自动刷新设备信息
    LaunchedEffect(Unit) {
        deviceService.scanForDevices()
        while (true) {
            delay(5 * 60 * 1000L) // 每5分钟刷新一次
            deviceService.scanForDevices()
        }
    }

    val refreshDeviceInfo: () -> Unit = {
        GlobalScope.launch {
            isRefreshing = true
            try {
                // 重新扫描设备
                deviceService.scanForDevices()
                // 等待扫描完成
                delay(500)
                
                // 如果有连接的设备，重新获取设备详细信息
                val currentDevices = deviceService.connectedDevices.value
                if (currentDevices.isNotEmpty()) {
                    val deviceInfo = deviceService.getDeviceInfo(currentDevices.first())
                    if (deviceInfo != null) {
                        // 这里设备信息会通过 StateFlow 自动更新UI
                        println("设备信息已刷新: ${deviceInfo.model}")
                    }
                }
            } catch (e: Exception) {
                println("刷新设备信息失败: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    Card(
        modifier = modifier.fillMaxSize().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "设备概览",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(
                    onClick = refreshDeviceInfo,
                    enabled = !isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新设备信息",
                        tint = if (isRefreshing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在获取设备信息...")
                    }
                }
            } else if (!deviceInfo.isConnected || connectedDevices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "未检测到设备",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "未检测到设备",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "请连接Android设备并开启USB调试",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DeviceHeaderCard(deviceInfo)
                    }
                    
                    item {
                        SystemInfoCard(deviceInfo, connectedDevices)
                    }
                    
                    item {
                        StorageInfoCard(deviceInfo)
                    }
                    
                    item {
                        SecurityInfoCard(deviceInfo)
                    }
                    
                    item {
                        HardwareInfoCard(deviceInfo)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceHeaderCard(deviceInfo: DeviceInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 设备Logo
            val logoResource = getDeviceLogo(deviceInfo.manufacturer, deviceInfo.model)
            if (logoResource != null) {
                Surface(
                    modifier = Modifier.size(56.dp), // 增大Logo尺寸
                    shape = RoundedCornerShape(16.dp), // 增大圆角
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Image(
                        painter = painterResource(logoResource),
                        contentDescription = "${deviceInfo.manufacturer} Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                // 通用设备图标
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "设备",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceInfo.model,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = deviceInfo.manufacturer,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Android ${deviceInfo.androidVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SystemInfoCard(deviceInfo: DeviceInfo, connectedDevices: List<String>) {
    val rebootService = remember { RebootService() }
    var showRebootMenu by remember { mutableStateOf(false) }
    var availableRebootModes by remember { mutableStateOf<List<RebootService.RebootMode>>(emptyList()) }
    var isLoadingRebootModes by remember { mutableStateOf(false) }
    
    // 获取当前设备ID
    val currentDeviceId = connectedDevices.firstOrNull() ?: ""
    
    // 加载可用的重启选项
    LaunchedEffect(currentDeviceId) {
        if (currentDeviceId.isNotEmpty()) {
            isLoadingRebootModes = true
            availableRebootModes = rebootService.getAvailableRebootModes(currentDeviceId)
            isLoadingRebootModes = false
        }
    }
    
    InfoCard(
        title = "系统信息",
        icon = Icons.Default.Info
    ) {
        // 高级重启选项按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(
                    onClick = { showRebootMenu = true },
                    enabled = !isLoadingRebootModes && availableRebootModes.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "高级重启选项",
                        tint = if (availableRebootModes.isNotEmpty()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 重启选项下拉菜单
                DropdownMenu(
                    expanded = showRebootMenu,
                    onDismissRequest = { showRebootMenu = false }
                ) {
                    availableRebootModes.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            onClick = {
                                showRebootMenu = false
                                // 执行重启操作
                                if (currentDeviceId.isNotEmpty()) {
                                    // 在协程中执行重启操作
                                    kotlinx.coroutines.GlobalScope.launch {
                                        try {
                                            rebootService.executeReboot(currentDeviceId, mode)
                                        } catch (e: Exception) {
                                            println("重启操作失败: ${e.message}")
                                        }
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mode) {
                                        RebootService.RebootMode.NORMAL -> Icons.Default.Refresh
                                        RebootService.RebootMode.RECOVERY -> Icons.Default.Build
                                        RebootService.RebootMode.BOOTLOADER -> Icons.Default.DeveloperMode
                                        RebootService.RebootMode.FASTBOOT -> Icons.Default.FlashOn
                                        RebootService.RebootMode.DOWNLOAD -> Icons.Default.Download
                                        RebootService.RebootMode.SAFE_MODE -> Icons.Default.Security
                                        RebootService.RebootMode.POWER_OFF -> Icons.Default.PowerOff
                                    },
                                    contentDescription = mode.displayName,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Root权限提示
        if (availableRebootModes.isEmpty()) {
            Text(
                text = "需要Root权限",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // 系统运行状态
        SystemStatusSection(deviceInfo)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // CPU信息
        ProcessorInfoRow(deviceInfo.cpuInfo)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 系统详细信息
        InfoRow("构建版本", deviceInfo.buildNumber, Icons.Default.Build)
        InfoRow("安全补丁", deviceInfo.securityPatch, Icons.Default.Security)
        InfoRow("序列号", deviceInfo.serialNumber, Icons.Default.Tag)
        InfoRow("已安装应用", "${deviceInfo.installedApps} 个", Icons.Default.Apps)
    }
}

@Composable
private fun SystemStatusSection(deviceInfo: DeviceInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8AADF4).copy(alpha = 0.1f) // Macchiato blue background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "运行状态",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF8AADF4) // Macchiato blue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "系统运行状态",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "开机时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = deviceInfo.uptime,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 运行状态指示器
                Surface(
                    color = Color(0xFFA6DA95), // Macchiato green
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "运行中",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageInfoCard(deviceInfo: DeviceInfo) {
    InfoCard(
        title = "存储信息",
        icon = Icons.Default.Storage
    ) {
        InfoRow("总存储", deviceInfo.totalStorage, Icons.Default.Storage)
        InfoRow("可用空间", deviceInfo.availableStorage, Icons.Default.FolderOpen)
        InfoRow("已用空间", deviceInfo.usedStorage, Icons.Default.Folder)
    }
}

@Composable
private fun SecurityInfoCard(deviceInfo: DeviceInfo) {
    InfoCard(
        title = "安全状态",
        icon = Icons.Default.Security
    ) {
        InfoRow("Bootloader", deviceInfo.bootloaderStatus, Icons.Default.Lock)
        InfoRow(
            "Root状态", 
            if (deviceInfo.isRooted) "已Root" else "未Root",
            if (deviceInfo.isRooted) Icons.Default.Warning else Icons.Default.CheckCircle
        )
    }
}

@Composable
private fun HardwareInfoCard(deviceInfo: DeviceInfo) {
    InfoCard(
        title = "硬件信息",
        icon = Icons.Default.Memory
    ) {
        InfoRow("内存", deviceInfo.ramInfo, Icons.Default.Memory)
        InfoRow("电池电量", deviceInfo.batteryLevel, Icons.Default.BatteryFull)
    }
}

@Composable
private fun ProcessorInfoRow(cpuInfo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 处理器品牌图标
                val (brandIcon, brandColor) = getProcessorBrandInfo(cpuInfo)
                Icon(
                    imageVector = brandIcon,
                    contentDescription = "处理器",
                    modifier = Modifier.size(20.dp),
                    tint = brandColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "处理器",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 处理器详细信息
            Text(
                text = cpuInfo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // 如果包含品牌信息，显示品牌标签
            val brandLabel = getBrandLabel(cpuInfo)
            if (brandLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = getBrandColor(cpuInfo).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = brandLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = getBrandColor(cpuInfo)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 辅助函数
fun getProcessorBrandInfo(cpuInfo: String): Pair<ImageVector, Color> {
    return when {
        cpuInfo.contains("高通", ignoreCase = true) -> 
            Pair(Icons.Default.Memory, Color(0xFFA6DA95)) // Macchiato green
        cpuInfo.contains("联发科", ignoreCase = true) -> 
            Pair(Icons.Default.Memory, Color(0xFF8AADF4)) // Macchiato blue
        cpuInfo.contains("华为海思", ignoreCase = true) -> 
            Pair(Icons.Default.Memory, Color(0xFFF5A97F)) // Macchiato peach
        cpuInfo.contains("三星", ignoreCase = true) -> 
            Pair(Icons.Default.Memory, Color(0xFFC6A0F6)) // Macchiato mauve
        cpuInfo.contains("紫光展锐", ignoreCase = true) -> 
            Pair(Icons.Default.Memory, Color(0xFF8BD5CA)) // Macchiato teal
        else -> 
            Pair(Icons.Default.Memory, Color(0xFFA5ADCB)) // Macchiato subtext0
    }
}

fun getBrandLabel(cpuInfo: String): String {
    return when {
        cpuInfo.contains("高通", ignoreCase = true) -> "Qualcomm"
        cpuInfo.contains("联发科", ignoreCase = true) -> "MediaTek"
        cpuInfo.contains("华为海思", ignoreCase = true) -> "HiSilicon"
        cpuInfo.contains("三星", ignoreCase = true) -> "Samsung"
        cpuInfo.contains("紫光展锐", ignoreCase = true) -> "UNISOC"
        else -> ""
    }
}

fun getBrandColor(cpuInfo: String): Color {
    return when {
        cpuInfo.contains("高通", ignoreCase = true) -> Color(0xFFA6DA95) // Macchiato green
        cpuInfo.contains("联发科", ignoreCase = true) -> Color(0xFF8AADF4) // Macchiato blue
        cpuInfo.contains("华为海思", ignoreCase = true) -> Color(0xFFF5A97F) // Macchiato peach
        cpuInfo.contains("三星", ignoreCase = true) -> Color(0xFFC6A0F6) // Macchiato mauve
        cpuInfo.contains("紫光展锐", ignoreCase = true) -> Color(0xFF8BD5CA) // Macchiato teal
        else -> Color(0xFFA5ADCB) // Macchiato subtext0
    }
}

// 获取设备Logo资源
 fun getDeviceLogo(manufacturer: String, model: String): String? {
     val manufacturerLower = manufacturer.lowercase()
     val modelLower = model.lowercase()
     
     return when {
         manufacturerLower.contains("oneplus") -> "logo/OnePlus.png"
         manufacturerLower.contains("samsung") -> "logo/Samsung.jpg"
         manufacturerLower.contains("xiaomi") -> "logo/Xiaomi.png"
         //gemini逻辑好怪
         manufacturerLower.contains("redmi") || modelLower.contains("redmi") -> "logo/Redmi.png"
         manufacturerLower.contains("oppo") -> "logo/Oppo.png"
         manufacturerLower.contains("vivo") -> "logo/Vivo.png"
         manufacturerLower.contains("realme") -> "logo/realme.png"
         manufacturerLower.contains("iqoo") || modelLower.contains("iqoo") -> "logo/iqoo.png"
         manufacturerLower.contains("lenovo") -> "logo/Lenovo.png"
         manufacturerLower.contains("motorola") -> "logo/Motorola.png"
         manufacturerLower.contains("sony") -> "logo/Sony.png"
         manufacturerLower.contains("meizu") -> "logo/meizu.png"
         manufacturerLower.contains("google") || modelLower.contains("pixel") -> "logo/google.png"
         else -> "logo/google.png"
     }
 }