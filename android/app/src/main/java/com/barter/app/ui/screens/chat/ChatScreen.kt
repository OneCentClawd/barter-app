package com.barter.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barter.app.ui.components.AvatarImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            uiState.otherUserId?.let { onNavigateToUserProfile(it) }
                        }
                    ) {
                        AvatarImage(
                            avatarUrl = uiState.otherUserAvatar,
                            name = uiState.otherUserName ?: "用户",
                            userId = uiState.otherUserId ?: 0L,
                            size = 36.dp,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(uiState.otherUserName ?: "聊天")
                    }
                },
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
        ) {
            // 连接状态提示条
            when (uiState.connectionStatus) {
                ConnectionStatus.DISCONNECTED, ConnectionStatus.RECONNECTING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFA000))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (uiState.connectionStatus == ConnectionStatus.RECONNECTING) 
                                "连接已断开，正在重连..." else "连接已断开",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                ConnectionStatus.FAILED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD32F2F))
                            .clickable { viewModel.retryConnection() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "连接失败，点击重试",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                ConnectionStatus.CONNECTED -> {
                    // 显示"已恢复连接"提示
                    if (uiState.showConnectionRestored) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4CAF50))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "已恢复连接",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        content = message.content,
                        isMe = message.isMe,
                        avatar = message.senderAvatar,
                        senderId = message.senderId,
                        senderName = message.senderName,
                        createdAt = message.createdAt,
                        onAvatarClick = {
                            if (!message.isMe) {
                                onNavigateToUserProfile(message.senderId)
                            }
                        }
                    )
                }
                
                // 正在输入提示
                if (uiState.isOtherTyping) {
                    item {
                        Row(
                            modifier = Modifier.padding(start = 44.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.otherUserName ?: "对方"}正在输入...",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { newText ->
                        messageText = newText
                        if (newText.isNotEmpty()) {
                            viewModel.onTyping()
                        } else {
                            viewModel.onStopTyping()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.onStopTyping()
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && !uiState.isSending
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "发送",
                        tint = if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    content: String,
    isMe: Boolean,
    avatar: String?,
    senderId: Long,
    senderName: String,
    createdAt: String?,
    onAvatarClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            if (!isMe) {
                AvatarImage(
                    avatarUrl = avatar,
                    name = senderName,
                    userId = senderId,
                    size = 36.dp,
                    fontSize = 14.sp,
                    onClick = onAvatarClick
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = content,
                    color = if (isMe) Color.White else Color.Black,
                    fontSize = 15.sp
                )
            }

            if (isMe) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        // 时间显示
        createdAt?.let { time ->
            Text(
                text = formatMessageTime(time),
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(
                    start = if (!isMe) 44.dp else 0.dp,
                    top = 2.dp
                )
            )
        }
    }
}

private fun formatMessageTime(timeStr: String): String {
    return try {
        // 解析时间字符串（服务器返回的是 UTC 时间）
        val dateTime = java.time.LocalDateTime.parse(timeStr.replace(" ", "T").take(19))
        // 将 UTC 时间转换为本地时间
        val utcZoned = dateTime.atZone(java.time.ZoneId.of("UTC"))
        val localDateTime = utcZoned.withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
        
        val now = java.time.LocalDateTime.now()
        val today = now.toLocalDate()
        val messageDate = localDateTime.toLocalDate()
        
        when {
            messageDate == today -> localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            messageDate == today.minusDays(1) -> "昨天 " + localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            messageDate.year == today.year -> localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"))
            else -> localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        }
    } catch (e: Exception) {
        timeStr.take(16).replace("T", " ")
    }
}
