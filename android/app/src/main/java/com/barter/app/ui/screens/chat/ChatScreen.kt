package com.barter.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    onNavigateBack: () -> Unit,
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
                title = { Text(uiState.otherUserName ?: "聊天") },
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
                        avatar = message.senderAvatar
                    )
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
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
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
    avatar: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            val avatarUrl = avatar?.let {
                if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
            }
            AsyncImage(
                model = avatarUrl ?: "https://via.placeholder.com/40",
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
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
}
