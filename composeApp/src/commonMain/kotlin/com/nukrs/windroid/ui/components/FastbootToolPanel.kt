package com.nukrs.windroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nukrs.windroid.core.service.DeviceService
import kotlinx.coroutines.launch
import java.io.File

/**
 * Fastboot工具面板
 * 提供完整的Fastboot功能界面
 */
@Composable
fun FastbootToolPanel(
    deviceService: DeviceService,
    modifier: Modifier = Modifier
) {
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var commandOutput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    var customCommand by remember { mutableStateOf("") }
    var selectedImageFile by remember { mutableStateOf<String?>(null) }
    
    val fastbootDevices by deviceService.fastbootDevices.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 每次打开Fastboot页面时自动扫描设备
    LaunchedEffect(Unit) {
        commandOutput += "=== Fastboot工具已启动 ===\n"
        commandOutput += "正在自动扫描Fastboot设备...\n"
        scope.launch {
            deviceService.scanForFastbootDevices()
            commandOutput += "Fastboot设备扫描完成\n"
            commandOutput += "提示: 请确保设备已进入Fastboot模式\n"
        }
    }
    
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 标题区域
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Fastboot 刷机工具",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // 左侧控制面板
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 设备选择区域
                    FastbootDeviceSelectionSection(
                        devices = fastbootDevices,
                        selectedDevice = selectedDevice,
                        onDeviceSelected = { selectedDevice = it },
                        onRefreshDevices = {
                            scope.launch {
                                commandOutput += "正在扫描Fastboot设备...\n"
                                deviceService.scanForFastbootDevices()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 快速操作区域
                    FastbootQuickActionsSection(
                        selectedDevice = selectedDevice,
                        isExecuting = isExecuting,
                        onExecutionStart = { isExecuting = true },
                        onExecutionEnd = { isExecuting = false },
                        onOutputUpdate = { output -> 
                            commandOutput += "$output\n"
                        },
                        deviceService = deviceService
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 镜像刷入区域
                    FastbootFlashSection(
                        selectedDevice = selectedDevice,
                        selectedImageFile = selectedImageFile,
                        onImageFileSelected = { selectedImageFile = it },
                        isExecuting = isExecuting,
                        onExecutionStart = { isExecuting = true },
                        onExecutionEnd = { isExecuting = false },
                        onOutputUpdate = { output -> 
                            commandOutput += "$output\n"
                        },
                        deviceService = deviceService
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 自定义命令区域
                    FastbootCustomCommandSection(
                        customCommand = customCommand,
                        onCommandChange = { customCommand = it },
                        selectedDevice = selectedDevice,
                        isExecuting = isExecuting,
                        onExecutionStart = { isExecuting = true },
                        onExecutionEnd = { isExecuting = false },
                        onOutputUpdate = { output -> 
                            commandOutput += "$output\n"
                        },
                        deviceService = deviceService
                    )
                }
                
                // 右侧输出区域
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "命令输出",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            IconButton(
                                onClick = { commandOutput = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清空输出",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = if (commandOutput.isEmpty()) "等待命令执行..." else commandOutput,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (commandOutput.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FastbootDeviceSelectionSection(
    devices: List<String>,
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    onRefreshDevices: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fastboot设备",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = onRefreshDevices
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新设备列表"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (devices.isEmpty()) {
                Text(
                    text = "未检测到Fastboot设备\n请确保设备已进入Fastboot模式并正确连接",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    devices.forEach { device ->
                        FilterChip(
                            onClick = { 
                                onDeviceSelected(if (selectedDevice == device) null else device)
                            },
                            label = { Text(device) },
                            selected = selectedDevice == device,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FastbootQuickActionsSection(
    selectedDevice: String?,
    isExecuting: Boolean,
    onExecutionStart: () -> Unit,
    onExecutionEnd: () -> Unit,
    onOutputUpdate: (String) -> Unit,
    deviceService: DeviceService
) {
    val scope = rememberCoroutineScope()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val quickActions = listOf(
                "devices" to "检测设备",
                "getvar all" to "获取设备信息",
                "reboot" to "重启设备",
                "reboot-bootloader" to "重启到Bootloader",
                "oem unlock" to "解锁Bootloader"
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickActions.forEach { (command, description) ->
                    Button(
                        onClick = {
                            if (!isExecuting) {
                                scope.launch {
                                    onExecutionStart()
                                    onOutputUpdate("=== 执行Fastboot命令 ===")
                                    onOutputUpdate("命令: fastboot $command")
                                    try {
                                        val result = deviceService.executeCustomFastbootCommand(command)
                                        onOutputUpdate("执行结果:")
                                        onOutputUpdate(result)
                                    } catch (e: Exception) {
                                        onOutputUpdate("执行失败: ${e.message}")
                                    }
                                    onOutputUpdate("命令执行完成")
                                    onExecutionEnd()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExecuting
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(description)
                            if (isExecuting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FastbootFlashSection(
    selectedDevice: String?,
    selectedImageFile: String?,
    onImageFileSelected: (String?) -> Unit,
    isExecuting: Boolean,
    onExecutionStart: () -> Unit,
    onExecutionEnd: () -> Unit,
    onOutputUpdate: (String) -> Unit,
    deviceService: DeviceService
) {
    var showFileChooser by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "镜像刷入",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 文件选择
            OutlinedButton(
                onClick = { showFileChooser = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedImageFile?.let { 
                        File(it).name 
                    } ?: "选择镜像文件 (*.img, *.bin, *.zip)"
                )
            }
            
            // 显示选中文件的详细信息
            selectedImageFile?.let { filePath ->
                val file = File(filePath)
                if (file.exists()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "文件信息",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "文件名: ${file.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "大小: ${formatFileSize(file.length())}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "路径: ${file.absolutePath}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 分区选择
            val partitions = listOf(
                "boot" to "Boot分区 (内核镜像)",
                "recovery" to "Recovery分区 (恢复模式)",
                "system" to "System分区 (系统镜像)",
                "userdata" to "Userdata分区 (用户数据)",
                "cache" to "Cache分区 (缓存)",
                "vendor" to "Vendor分区 (厂商镜像)",
                "dtbo" to "DTBO分区 (设备树)",
                "vbmeta" to "VBMeta分区 (验证启动)"
            )
            
            Text(
                text = "选择目标分区",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                partitions.forEach { (partition, description) ->
                    Button(
                        onClick = {
                            if (!isExecuting && selectedImageFile != null && selectedDevice != null) {
                                scope.launch {
                                    onExecutionStart()
                                    onOutputUpdate("=== 开始刷入镜像到 $partition 分区 ===")
                                    onOutputUpdate("设备: $selectedDevice")
                                    onOutputUpdate("文件: ${File(selectedImageFile).name}")
                                    onOutputUpdate("分区: $partition")
                                    onOutputUpdate("正在执行: fastboot flash $partition \"$selectedImageFile\"")
                                    
                                    try {
                                        val command = "flash $partition \"$selectedImageFile\""
                                        val result = deviceService.executeCustomFastbootCommand(command)
                                        onOutputUpdate("刷入结果:")
                                        onOutputUpdate(result)
                                        
                                        if (result.contains("OKAY") || result.contains("finished")) {
                                            onOutputUpdate("✅ 镜像刷入成功!")
                                        } else if (result.contains("FAILED") || result.contains("error")) {
                                            onOutputUpdate("❌ 镜像刷入失败!")
                                        }
                                    } catch (e: Exception) {
                                        onOutputUpdate("❌ 刷入失败: ${e.message}")
                                    }
                                    
                                    onOutputUpdate("镜像刷入操作完成")
                                    onExecutionEnd()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExecuting && selectedImageFile != null && selectedDevice != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (partition) {
                                "boot", "recovery" -> MaterialTheme.colorScheme.primary
                                "system", "vendor" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "刷入到 $partition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                            
                            if (isExecuting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // 提示信息
            if (selectedDevice == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "请先选择Fastboot设备",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (selectedImageFile == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "请先选择要刷入的镜像文件",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    // 文件选择对话框
    if (showFileChooser) {
        FileChooserDialog(
            title = "选择镜像文件",
            fileExtensions = listOf("img", "bin", "zip"),
            onFileSelected = { filePath ->
                onImageFileSelected(filePath)
                showFileChooser = false
                onOutputUpdate("已选择文件: ${File(filePath).name}")
            },
            onDismiss = { showFileChooser = false }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

@Composable
private fun FastbootCustomCommandSection(
    customCommand: String,
    onCommandChange: (String) -> Unit,
    selectedDevice: String?,
    isExecuting: Boolean,
    onExecutionStart: () -> Unit,
    onExecutionEnd: () -> Unit,
    onOutputUpdate: (String) -> Unit,
    deviceService: DeviceService
) {
    val scope = rememberCoroutineScope()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "自定义命令",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = customCommand,
                onValueChange = onCommandChange,
                label = { Text("Fastboot命令 (不需要输入fastboot前缀)") },
                placeholder = { Text("例如: devices, getvar all, reboot") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    if (customCommand.isNotBlank()) {
                        scope.launch {
                            onExecutionStart()
                            onOutputUpdate("=== 执行Fastboot命令 ===")
                            onOutputUpdate("命令: fastboot $customCommand")
                            try {
                                val result = deviceService.executeCustomFastbootCommand(customCommand)
                                onOutputUpdate("执行结果:")
                                onOutputUpdate(result)
                            } catch (e: Exception) {
                                onOutputUpdate("执行失败: ${e.message}")
                            }
                            onOutputUpdate("命令执行完成")
                            onExecutionEnd()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting && customCommand.isNotBlank()
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("执行中...")
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("执行命令")
                }
            }
        }
    }
}