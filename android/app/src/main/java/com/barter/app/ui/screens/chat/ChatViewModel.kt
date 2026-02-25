package com.barter.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.local.TokenManager
import com.barter.app.data.remote.ChatWebSocketManager
import com.barter.app.data.repository.ChatRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long,
    val content: String,
    val isMe: Boolean,
    val senderId: Long,
    val senderName: String,
    val senderAvatar: String?,
    val createdAt: String?
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherUserName: String? = null,
    val otherUserId: Long? = null,
    val otherUserAvatar: String? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
    private val webSocketManager: ChatWebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: Long = 0
    private var currentUserId: Long = 0
    
    init {
        // 监听 WebSocket 消息
        viewModelScope.launch {
            webSocketManager.incomingMessages.collect { wsMessage ->
                // 只处理当前对话的消息
                if (wsMessage.conversationId == conversationId && wsMessage.type == "NEW_MESSAGE") {
                    val newMessage = ChatMessage(
                        id = wsMessage.message.id,
                        content = wsMessage.message.content,
                        isMe = wsMessage.message.senderId == currentUserId,
                        senderId = wsMessage.message.senderId,
                        senderName = wsMessage.message.senderNickname ?: "用户",
                        senderAvatar = wsMessage.message.senderAvatar,
                        createdAt = wsMessage.message.createdAt
                    )
                    // 避免重复添加
                    if (_uiState.value.messages.none { it.id == newMessage.id }) {
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + newMessage
                        )
                    }
                }
            }
        }
    }

    fun loadConversation(conversationId: Long) {
        this.conversationId = conversationId
        viewModelScope.launch {
            currentUserId = tokenManager.userId.first() ?: 0
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 连接 WebSocket
            if (!webSocketManager.isConnected()) {
                webSocketManager.connect()
            }

            when (val result = chatRepository.getConversationDetail(conversationId)) {
                is Result.Success -> {
                    val detail = result.data
                    val messages = detail.messages.map { msg ->
                        ChatMessage(
                            id = msg.id,
                            content = msg.content,
                            isMe = msg.senderId == currentUserId,
                            senderId = msg.senderId,
                            senderName = msg.senderNickname ?: "用户",
                            senderAvatar = msg.senderAvatar,
                            createdAt = msg.createdAt
                        )
                    }.sortedBy { it.createdAt ?: "" }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = messages,
                        otherUserName = detail.otherUser?.nickname ?: detail.otherUser?.username,
                        otherUserId = detail.otherUser?.id,
                        otherUserAvatar = detail.otherUser?.avatar
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun sendMessage(content: String) {
        val receiverId = _uiState.value.otherUserId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            
            when (val result = chatRepository.sendMessage(receiverId, content)) {
                is Result.Success -> {
                    // 添加新消息到列表
                    val newMessage = ChatMessage(
                        id = result.data.id,
                        content = content,
                        isMe = true,
                        senderId = currentUserId ?: 0L,
                        senderName = "我",
                        senderAvatar = null,
                        createdAt = java.time.LocalDateTime.now().toString()
                    )
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + newMessage
                    )
                    // AI 回复会通过 WebSocket 推送过来，不需要手动拉取
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // ViewModel 销毁时不断开 WebSocket，让它在 App 生命周期内保持连接
    }
}
