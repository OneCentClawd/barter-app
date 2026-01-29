package com.barter.app.ui.screens.main.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.BuildConfig
import com.barter.app.data.model.Conversation

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel = hiltViewModel(),
    onNavigateToChat: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.conversations.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无消息")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onNavigateToChat(conversation.id) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        val avatarUrl = conversation.otherUser.avatar?.let {
            if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
        }
        AsyncImage(
            model = avatarUrl ?: "https://via.placeholder.com/48",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.otherUser.nickname ?: conversation.otherUser.username,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.lastMessage?.content ?: "暂无消息",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 未读数
        conversation.unreadCount?.let { count ->
            if (count > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
