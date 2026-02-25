package com.barter.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.local.TokenManager
import com.barter.app.data.remote.ChatWebSocketManager
import com.barter.app.data.repository.ChatRepository
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherUserName: String? = null,
    val otherUserId: Long? = null,
    val otherUserAvatar: String? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val conversationId: Long? = null,
    val isOtherTyping: Boolean = false
)

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val webSocketManager: ChatWebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    private var targetUserId: Long = 0
    private var currentUserId: Long = 0
    
    init {
        // 监听 WebSocket 消息
        viewModelScope.launch {
            currentUserId = tokenManager.userId.first() ?: 0
            
            webSocketManager.incomingMessages.collect { wsMessage ->
                // 只处理 NEW_MESSAGE 类型
                if (wsMessage.type == "NEW_MESSAGE") {
                    val msg = wsMessage.message ?: return@collect
                    // 通过 senderId 判断是否是当前对话
                    if (msg.senderId == targetUserId) {
                        val newMessage = ChatMessage(
                            id = msg.id,
                            content = msg.content,
                            isMe = msg.senderId == currentUserId,
                            senderId = msg.senderId,
                            senderName = msg.senderNickname ?: "用户",
                            senderAvatar = msg.senderAvatar,
                            createdAt = msg.createdAt
                        )
                        // 避免重复添加
                        if (_uiState.value.messages.none { it.id == newMessage.id }) {
                            _uiState.value = _uiState.value.copy(
                                messages = _uiState.value.messages + newMessage,
                                conversationId = wsMessage.conversationId,
                                isOtherTyping = false // 收到消息时清除 typing 状态
                            )
                        }
                    }
                }
            }
        }
        
        // 监听 typing 状态
        viewModelScope.launch {
            webSocketManager.typingState.collect { event ->
                if (event.userId == targetUserId) {
                    _uiState.value = _uiState.value.copy(isOtherTyping = event.isTyping)
                }
            }
        }
    }

    fun initChat(userId: Long) {
        targetUserId = userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, otherUserId = userId)
            
            // 确保 WebSocket 连接（会自动处理重连）
            webSocketManager.ensureConnected()

            // 获取对方用户信息
            when (val result = userRepository.getProfile(userId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otherUserName = result.data.nickname ?: result.data.username,
                        otherUserAvatar = result.data.avatar
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otherUserName = "用户"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            
            when (val result = chatRepository.sendMessage(targetUserId, content)) {
                is Result.Success -> {
                    val newMessage = ChatMessage(
                        id = result.data.id,
                        content = content,
                        isMe = true,
                        senderId = currentUserId,
                        senderName = "我",
                        senderAvatar = null,
                        createdAt = java.time.LocalDateTime.now().toString()
                    )
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + newMessage
                    )
                    // AI 回复会通过 WebSocket 推送过来
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
}
