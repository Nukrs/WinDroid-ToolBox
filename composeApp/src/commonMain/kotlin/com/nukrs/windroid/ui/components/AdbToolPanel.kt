package com.nukrs.windroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

/**
 * ADB工具面板
 * 提供完整的ADB功能界面
 */
@Composable
fun AdbToolPanel(
    deviceService: DeviceService,
    modifier: Modifier = Modifier
) {
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var commandOutput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    var customCommand by remember { mutableStateOf("") }
    
    val connectedDevices by deviceService.connectedDevices.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 每次打开ADB页面时自动扫描设备
    LaunchedEffect(Unit) {
        commandOutput += "=== ADB工具已启动 ===\n"
        commandOutput += "正在自动扫描设备...\n"
        deviceService.scanForDevices()
        commandOutput += "设备扫描完成\n"
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
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ADB 调试工具",
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
                ) {
                    // 设备选择区域
                    DeviceSelectionSection(
                        devices = connectedDevices,
                        selectedDevice = selectedDevice,
                        onDeviceSelected = { selectedDevice = it },
                        onRefreshDevices = {
                            scope.launch {
                                deviceService.scanForDevices()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 自定义命令区域
                    CustomCommandSection(
                        customCommand = customCommand,
                        onCommandChange = { customCommand = it },
                        selectedDevice = selectedDevice,
                        deviceService = deviceService,
                        isExecuting = isExecuting,
                        onExecutionStart = { isExecuting = true },
                        onExecutionEnd = { isExecuting = false },
                        onOutputUpdate = { output -> 
                            commandOutput += "$output\n"
                        }
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
private fun DeviceSelectionSection(
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
                    text = "连接的设备",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(onClick = onRefreshDevices) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新设备列表"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (devices.isEmpty()) {
                Text(
                    text = "未检测到设备",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(devices) { device ->
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
private fun CustomCommandSection(
    customCommand: String,
    onCommandChange: (String) -> Unit,
    selectedDevice: String?,
    deviceService: DeviceService,
    isExecuting: Boolean,
    onExecutionStart: () -> Unit,
    onExecutionEnd: () -> Unit,
    onOutputUpdate: (String) -> Unit
) {
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
                text = "ADB命令执行",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = customCommand,
                onValueChange = onCommandChange,
                label = { Text("ADB命令 (不需要输入adb前缀)") },
                placeholder = { Text("例如: devices, shell, install app.apk") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting,
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    if (customCommand.isNotBlank()) {
                        scope.launch {
                            onExecutionStart()
                            try {
                                // 检查是否是shell命令
                                if (customCommand.trim().lowercase() == "shell") {
                                    onOutputUpdate("=== 打开ADB Shell ===")
                                    onOutputUpdate("正在启动PowerShell窗口执行 adb shell...")
                                    
                                    // 构建命令
                                    val shellCommand = if (selectedDevice != null) {
                                        "adb -s $selectedDevice shell"
                                    } else {
                                        "adb shell"
                                    }
                                    
                                    try {
                                        // 在新的PowerShell窗口中执行adb shell
                                        val processBuilder = ProcessBuilder(
                                            "powershell.exe", 
                                            "-Command", 
                                            "Start-Process powershell -ArgumentList '-NoExit', '-Command', '$shellCommand'"
                                        )
                                        processBuilder.start()
                                        onOutputUpdate("PowerShell窗口已打开，您可以在新窗口中进行交互式操作")
                                        onOutputUpdate("提示: 输入 'exit' 退出shell")
                                    } catch (e: Exception) {
                                        onOutputUpdate("打开PowerShell失败: ${e.message}")
                                    }
                                } else {
                                    // 普通命令执行
                                    onOutputUpdate("=== 执行ADB命令 ===")
                                    onOutputUpdate("命令: adb $customCommand")
                                    
                                    val result = deviceService.executeCustomCommand(customCommand)
                                    onOutputUpdate("输出:")
                                    onOutputUpdate(result)
                                }
                            } catch (e: Exception) {
                                onOutputUpdate("错误: ${e.message}")
                            } finally {
                                onExecutionEnd()
                            }
                        }
                    }
                },
                enabled = customCommand.isNotBlank() && !isExecuting,
                modifier = Modifier.fillMaxWidth()
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
                        imageVector = if (customCommand.trim().lowercase() == "shell") {
                            Icons.Default.Terminal
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (customCommand.trim().lowercase() == "shell") {
                            "打开Shell窗口"
                        } else {
                            "执行命令"
                        }
                    )
                }
            }
            
            // 添加常用命令快捷按钮
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "常用命令",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val commonCommands = listOf(
                    "devices" to "设备列表",
                    "shell" to "Shell",
                    "logcat" to "日志",
                    "install" to "安装",
                    "uninstall" to "卸载"
                )
                
                items(commonCommands) { (command, label) ->
                    FilterChip(
                        onClick = { onCommandChange(command) },
                        label = { Text(label) },
                        selected = false,
                        enabled = !isExecuting
                    )
                }
            }
        }
    }
}