package com.nukrs.windroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nukrs.windroid.ui.components.ToolCard
import com.nukrs.windroid.ui.components.StatusBar
import com.nukrs.windroid.ui.components.DeviceOverviewPanel
import com.nukrs.windroid.ui.components.ThemeToggleButton
import com.nukrs.windroid.ui.components.AboutButton
import com.nukrs.windroid.ui.components.AdbToolPanel
import com.nukrs.windroid.ui.components.FastbootToolPanel
import com.nukrs.windroid.data.model.FlashingTool
import com.nukrs.windroid.core.service.DeviceService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTool by remember { mutableStateOf<FlashingTool?>(null) }
    var selectedView by remember { mutableStateOf("overview") } // 默认显示概览
    val deviceService = remember { DeviceService() }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = { Text("WinDroid Toolbox") },
            actions = {
                ThemeToggleButton()
                AboutButton()
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // 左侧导航面板
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // 概览按钮
                NavigationCard(
                    title = "设备概览",
                    icon = Icons.Default.Dashboard,
                    isSelected = selectedView == "overview",
                    onClick = { 
                        selectedView = "overview"
                        selectedTool = null
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "刷机工具",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getFlashingTools()) { tool ->
                        ToolCard(
                            tool = tool,
                            isSelected = selectedTool == tool,
                            onClick = { 
                                selectedTool = tool
                                selectedView = "tool"
                            }
                        )
                    }
                }
            }
            
            // 右侧详情面板
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(16.dp)
            ) {
                when (selectedView) {
                    "overview" -> {
                        DeviceOverviewPanel(deviceService = deviceService)
                    }
                    "tool" -> {
                        if (selectedTool != null) {
                            ToolDetailPanel(tool = selectedTool!!)
                        } else {
                            DeviceOverviewPanel(deviceService = deviceService)
                        }
                    }
                    else -> {
                        DeviceOverviewPanel(deviceService = deviceService)
                    }
                }
            }
        }
        
        // 底部状态栏
        StatusBar()
    }
}

@Composable
fun NavigationCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun ToolDetailPanel(tool: FlashingTool) {
    // 如果是ADB工具，显示专门的ADB面板
    if (tool.id == "adb") {
        AdbToolPanel(
            deviceService = remember { DeviceService() },
            modifier = Modifier.fillMaxSize()
        )
    } else if (tool.id == "fastboot") {
        // 如果是Fastboot工具，显示专门的Fastboot面板
        FastbootToolPanel(
            deviceService = remember { DeviceService() },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 其他工具显示通用面板
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    tool.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 功能按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* TODO: 实现功能 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("开始${tool.name}")
                    }
                    
                    OutlinedButton(
                        onClick = { /* TODO: 打开设置 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("设置")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作日志区域
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "操作日志",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "等待操作...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getFlashingTools(): List<FlashingTool> {
    return listOf(
        FlashingTool(
            id = "fastboot",
            name = "Fastboot刷机",
            description = "使用Fastboot命令刷入系统镜像、Recovery等",
            icon = Icons.Default.FlashOn,
            category = "刷机工具"
        ),
        FlashingTool(
            id = "adb",
            name = "ADB工具",
            description = "Android调试桥，用于设备调试和文件传输",
            icon = Icons.Default.DeveloperMode,
            category = "调试工具"
        )
    )
}