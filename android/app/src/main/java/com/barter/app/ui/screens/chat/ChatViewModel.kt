package com.barter.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.local.TokenManager
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
    val senderAvatar: String?
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherUserName: String? = null,
    val otherUserId: Long? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: Long = 0
    private var currentUserId: Long = 0

    fun loadConversation(conversationId: Long) {
        this.conversationId = conversationId
        viewModelScope.launch {
            currentUserId = tokenManager.userId.first() ?: 0
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = chatRepository.getConversationDetail(conversationId)) {
                is Result.Success -> {
                    val detail = result.data
                    val messages = detail.messages.map { msg ->
                        ChatMessage(
                            id = msg.id,
                            content = msg.content,
                            isMe = msg.senderId == currentUserId,
                            senderAvatar = msg.senderAvatar
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = messages,
                        otherUserName = detail.otherUser?.nickname ?: detail.otherUser?.username,
                        otherUserId = detail.otherUser?.id
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
                        senderAvatar = null
                    )
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + newMessage
                    )
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
