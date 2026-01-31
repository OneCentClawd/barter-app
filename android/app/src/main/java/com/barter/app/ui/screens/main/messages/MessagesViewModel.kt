package com.barter.app.ui.screens.main.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.Conversation
import com.barter.app.data.model.PublicProfile
import com.barter.app.data.remote.ApiService
import com.barter.app.data.repository.ChatRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val adminUser: PublicProfile? = null,
    val error: String? = null
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = chatRepository.getConversations()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        conversations = result.data.content
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

    fun loadAdminUser() {
        viewModelScope.launch {
            try {
                val response = apiService.getAdminUser()
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        adminUser = response.body()!!.data
                    )
                }
            } catch (e: Exception) {
                // 忽略错误，不显示联系客服按钮
            }
        }
    }
}
