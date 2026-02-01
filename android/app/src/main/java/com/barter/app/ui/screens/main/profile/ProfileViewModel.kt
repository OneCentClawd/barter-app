package com.barter.app.ui.screens.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.ItemListItem
import com.barter.app.data.repository.AuthRepository
import com.barter.app.data.repository.ItemRepository
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userId: Long? = null,
    val username: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val rating: Double? = null,
    val ratingCount: Int = 0,
    val itemCount: Int = 0,
    val tradeCount: Int = 0,
    val myItems: List<ItemListItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 从本地获取基本信息
            val userId = authRepository.getUserId().first()
            val username = authRepository.getUsername().first()
            val nickname = authRepository.getNickname().first()
            val avatar = authRepository.getAvatar().first()

            _uiState.value = _uiState.value.copy(
                userId = userId,
                username = username,
                nickname = nickname,
                avatar = avatar
            )

            // 从服务器获取详细信息
            when (val result = userRepository.getMyProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        nickname = user.nickname,
                        avatar = user.avatar,
                        rating = user.rating,
                        ratingCount = user.ratingCount ?: 0,
                        itemCount = user.itemCount ?: 0,
                        tradeCount = user.tradeCount ?: 0
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is Result.Loading -> {}
            }

            // 加载我的物品
            when (val result = itemRepository.getMyItems()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myItems = result.data.content
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
