package com.barter.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barter.app.data.model.LoginRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRecordsScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoginRecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadRecords() }) {
                            Text("重试")
                        }
                    }
                }
            }
            uiState.records.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无登录记录", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.records) { record ->
                        LoginRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginRecordCard(record: LoginRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 设备图标
            Icon(
                imageVector = when (record.deviceType) {
                    "Android" -> Icons.Default.PhoneAndroid
                    "iOS" -> Icons.Default.PhoneIphone
                    "Windows", "Mac" -> Icons.Default.Computer
                    else -> Icons.Default.Devices
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (record.success) MaterialTheme.colorScheme.primary else Color.Red
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.deviceType ?: "未知设备",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (record.success) {
                        Text(
                            text = "成功",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        Text(
                            text = "失败",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "IP: ${record.ipAddress ?: "未知"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                if (!record.failReason.isNullOrBlank()) {
                    Text(
                        text = "原因: ${record.failReason}",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
                
                record.loginTime?.let { time ->
                    Text(
                        text = formatTime(time),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun formatTime(timeStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(timeStr, formatter)
        dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } catch (e: Exception) {
        timeStr
    }
}
