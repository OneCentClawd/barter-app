package com.barter.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.AllowRequest
import com.barter.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminSettingsUiState(
    val isLoading: Boolean = false,
    val allowUserChat: Boolean = false,
    val allowUserViewItems: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getAdminConfig()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allowUserChat = data.allowUserChat,
                        allowUserViewItems = data.allowUserViewItems
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "获取配置失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "获取配置失败: ${e.message}"
                )
            }
        }
    }

    fun setAllowUserChat(allow: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val response = apiService.setAllowUserChat(AllowRequest(allow))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allowUserChat = allow,
                        message = if (allow) "已开启用户间聊天" else "已关闭用户间聊天"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "设置失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "设置失败: ${e.message}"
                )
            }
        }
    }

    fun setAllowUserViewItems(allow: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val response = apiService.setAllowUserViewItems(AllowRequest(allow))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allowUserViewItems = allow,
                        message = if (allow) "已开启用户物品可见" else "已关闭用户物品可见"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "设置失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "设置失败: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
