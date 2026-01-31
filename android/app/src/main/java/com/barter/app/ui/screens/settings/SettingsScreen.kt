package com.barter.app.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToLoginRecords: () -> Unit = {},
    onNavigateToAdminSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    // 处理密码修改结果
    LaunchedEffect(uiState.passwordChangeResult) {
        when (val result = uiState.passwordChangeResult) {
            is PasswordChangeResult.Success -> {
                Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show()
                showChangePasswordDialog = false
                viewModel.clearPasswordChangeResult()
            }
            is PasswordChangeResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                viewModel.clearPasswordChangeResult()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ========== 管理员设置（仅管理员可见）==========
            if (uiState.isAdmin) {
                SettingsSectionHeader("管理员")
                
                SettingsItem(
                    title = "管理员设置",
                    subtitle = "系统配置管理",
                    onClick = onNavigateToAdminSettings
                )
                
                Divider()
            }

            // ========== 账号安全 ==========
            SettingsSectionHeader("账号安全")
            
            SettingsItem(
                title = "修改密码",
                onClick = { showChangePasswordDialog = true }
            )
            
            Divider()
            
            SettingsItem(
                title = "登录记录",
                subtitle = "查看账号登录历史",
                onClick = onNavigateToLoginRecords
            )
            
            Divider()
            
            // ========== 隐私设置 ==========
            SettingsSectionHeader("隐私设置")
            
            SettingsSwitchItem(
                title = "允许他人查看手机号",
                checked = uiState.settings.showPhoneToOthers,
                onCheckedChange = { viewModel.updateSetting(showPhoneToOthers = it) }
            )
            
            Divider()
            
            SettingsSwitchItem(
                title = "允许陌生人私信",
                checked = uiState.settings.allowStrangersMessage,
                onCheckedChange = { viewModel.updateSetting(allowStrangersMessage = it) }
            )
            
            Divider()
            
            // ========== 通知设置 ==========
            SettingsSectionHeader("通知设置")
            
            SettingsSwitchItem(
                title = "新消息通知",
                checked = uiState.settings.notifyNewMessage,
                onCheckedChange = { viewModel.updateSetting(notifyNewMessage = it) }
            )
            
            Divider()
            
            SettingsSwitchItem(
                title = "交易状态更新",
                checked = uiState.settings.notifyTradeUpdate,
                onCheckedChange = { viewModel.updateSetting(notifyTradeUpdate = it) }
            )
            
            Divider()
            
            SettingsSwitchItem(
                title = "系统公告",
                checked = uiState.settings.notifySystemAnnouncement,
                onCheckedChange = { viewModel.updateSetting(notifySystemAnnouncement = it) }
            )
            
            Divider()
            
            // ========== 其他 ==========
            SettingsSectionHeader("其他")
            
            SettingsItem(
                title = "清除缓存",
                subtitle = getCacheSize(context),
                onClick = { showClearCacheDialog = true }
            )
            
            Divider()
            
            SettingsItem(
                title = "关于易物",
                subtitle = "版本 1.0.0",
                onClick = { showAboutDialog = true }
            )
            
            Divider()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 退出登录按钮
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("退出登录")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ========== 对话框 ==========
    
    // 退出登录确认
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 修改密码对话框
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("修改密码") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("当前密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("新密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("确认新密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            oldPassword.isBlank() || newPassword.isBlank() -> {
                                Toast.makeText(context, "请填写完整", Toast.LENGTH_SHORT).show()
                            }
                            newPassword != confirmPassword -> {
                                Toast.makeText(context, "两次密码不一致", Toast.LENGTH_SHORT).show()
                            }
                            newPassword.length < 6 -> {
                                Toast.makeText(context, "密码至少6位", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                viewModel.changePassword(oldPassword, newPassword)
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 清除缓存确认
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("清除缓存") },
            text = { Text("确定要清除所有缓存吗？这不会影响您的账号数据。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearCache(context)
                        showClearCacheDialog = false
                        Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于易物") },
            text = {
                Column {
                    Text("易物 - 闲置物品交换平台")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("版本：1.0.0", fontSize = 14.sp)
                    Text("开发者：Barter Team", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "让闲置物品流动起来，以物换物，绿色环保。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun getCacheSize(context: Context): String {
    val cacheDir = context.cacheDir
    val size = getDirSize(cacheDir)
    return formatFileSize(size)
}

private fun getDirSize(dir: File): Long {
    var size: Long = 0
    if (dir.isDirectory) {
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
    }
    return size
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}

@OptIn(ExperimentalCoilApi::class)
private fun clearCache(context: Context) {
    // 清除应用缓存
    context.cacheDir.deleteRecursively()
    // 清除 Coil 图片缓存
    context.imageLoader.diskCache?.clear()
    context.imageLoader.memoryCache?.clear()
}
