package com.nukrs.windroid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * 文件选择器对话框
 */
@Composable
fun FileChooserDialog(
    title: String = "选择文件",
    initialDirectory: String = System.getProperty("user.home"),
    fileExtensions: List<String> = emptyList(), // 例如: listOf("img", "bin", "zip")
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDirectory by remember { mutableStateOf(File(initialDirectory)) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()
    
    // 获取系统所有可用的磁盘驱动器
    val availableDrives = remember {
        File.listRoots().toList()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 磁盘驱动器选择
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "磁盘驱动器",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableDrives) { drive ->
                                DriveItem(
                                    drive = drive,
                                    isSelected = currentDirectory.absolutePath.startsWith(drive.absolutePath),
                                    onClick = {
                                        currentDirectory = drive
                                        selectedFile = null
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 当前路径显示
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentDirectory.absolutePath,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 导航按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 上级目录按钮
                    OutlinedButton(
                        onClick = {
                            currentDirectory.parentFile?.let { parent ->
                                currentDirectory = parent
                                selectedFile = null
                            }
                        },
                        enabled = currentDirectory.parentFile != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("上级目录")
                    }
                    
                    // 主目录按钮
                    OutlinedButton(
                        onClick = {
                            currentDirectory = File(System.getProperty("user.home"))
                            selectedFile = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("主目录")
                    }
                    
                    // 桌面按钮
                    OutlinedButton(
                        onClick = {
                            val desktop = File(System.getProperty("user.home"), "Desktop")
                            if (desktop.exists()) {
                                currentDirectory = desktop
                                selectedFile = null
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("桌面")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 文件列表
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val files = try {
                            currentDirectory.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }) ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                        
                        items(files) { file ->
                            FileItem(
                                file = file,
                                isSelected = selectedFile == file,
                                fileExtensions = fileExtensions,
                                onClick = {
                                    if (file.isDirectory) {
                                        currentDirectory = file
                                        selectedFile = null
                                    } else {
                                        selectedFile = file
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选中文件显示
                if (selectedFile != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "已选择: ${selectedFile!!.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "大小: ${formatFileSize(selectedFile!!.length())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 底部按钮
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 系统文件选择器按钮
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val fileChooser = JFileChooser().apply {
                                        currentDirectory = currentDirectory
                                        dialogTitle = title
                                        fileSelectionMode = JFileChooser.FILES_ONLY
                                        
                                        // 设置文件过滤器
                                        if (fileExtensions.isNotEmpty()) {
                                            val description = "支持的文件 (${fileExtensions.joinToString(", ") { "*.$it" }})"
                                            val filter = FileNameExtensionFilter(description, *fileExtensions.toTypedArray())
                                            fileFilter = filter
                                        }
                                    }
                                    
                                    val result = fileChooser.showOpenDialog(null)
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        val selectedSystemFile = fileChooser.selectedFile
                                        onFileSelected(selectedSystemFile.absolutePath)
                                    }
                                } catch (e: Exception) {
                                    println("系统文件选择器错误: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("使用系统文件选择器")
                    }
                    
                    // 主要操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        
                        Button(
                            onClick = {
                                selectedFile?.let { file ->
                                    onFileSelected(file.absolutePath)
                                }
                            },
                            enabled = selectedFile != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveItem(
    drive: File,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val driveLetter = drive.absolutePath.replace("\\", "")
    val driveInfo = try {
        val totalSpace = drive.totalSpace
        val freeSpace = drive.freeSpace
        val usedSpace = totalSpace - freeSpace
        val usagePercent = if (totalSpace > 0) (usedSpace * 100 / totalSpace).toInt() else 0
        
        Triple(formatFileSize(totalSpace), formatFileSize(freeSpace), usagePercent)
    } catch (e: Exception) {
        Triple("未知", "未知", 0)
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when {
                    driveLetter.startsWith("C") -> Icons.Default.Storage
                    driveLetter.contains("USB") || driveLetter.contains("移动") -> Icons.Default.Usb
                    else -> Icons.Default.Storage
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = driveLetter,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = driveInfo.first,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "可用: ${driveInfo.second}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 使用进度条显示磁盘使用情况
            if (driveInfo.third > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { driveInfo.third / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = when {
                        driveInfo.third > 90 -> MaterialTheme.colorScheme.error
                        driveInfo.third > 75 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }
}

@Composable
private fun FileItem(
    file: File,
    isSelected: Boolean,
    fileExtensions: List<String>,
    onClick: () -> Unit
) {
    val isValidFile = if (file.isFile && fileExtensions.isNotEmpty()) {
        val extension = file.extension.lowercase()
        fileExtensions.any { it.lowercase() == extension }
    } else {
        true
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                !isValidFile && file.isFile -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    file.isDirectory -> Icons.Default.Folder
                    file.extension.lowercase() in listOf("img", "bin", "zip", "tar", "gz") -> Icons.Default.Archive
                    file.extension.lowercase() in listOf("txt", "log", "md") -> Icons.Default.Description
                    file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp") -> Icons.Default.Image
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    !isValidFile && file.isFile -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    file.isDirectory -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        !isValidFile && file.isFile -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (file.isFile) {
                    Text(
                        text = formatFileSize(file.length()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (!isValidFile) 0.5f else 1f
                        )
                    )
                } else {
                    Text(
                        text = "文件夹",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            if (!isValidFile && file.isFile) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "不支持的文件类型",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    if (gb < 1024) return "%.1f GB".format(gb)
    val tb = gb / 1024.0
    return "%.1f TB".format(tb)
}