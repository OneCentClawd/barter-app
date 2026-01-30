package com.barter.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.UpdateSettingsRequest
import com.barter.app.data.model.UserSettings
import com.barter.app.data.repository.AuthRepository
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val passwordChangeResult: PasswordChangeResult? = null
)

sealed class PasswordChangeResult {
    object Success : PasswordChangeResult()
    data class Error(val message: String) : PasswordChangeResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userRepository.getSettings()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        settings = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
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

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            when (val result = userRepository.changePassword(oldPassword, newPassword)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        passwordChangeResult = PasswordChangeResult.Success
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        passwordChangeResult = PasswordChangeResult.Error(result.message)
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearPasswordChangeResult() {
        _uiState.value = _uiState.value.copy(passwordChangeResult = null)
    }

    fun updateSetting(
        showPhoneToOthers: Boolean? = null,
        allowStrangersMessage: Boolean? = null,
        notifyNewMessage: Boolean? = null,
        notifyTradeUpdate: Boolean? = null,
        notifySystemAnnouncement: Boolean? = null
    ) {
        // 先乐观更新 UI
        val currentSettings = _uiState.value.settings
        _uiState.value = _uiState.value.copy(
            settings = currentSettings.copy(
                showPhoneToOthers = showPhoneToOthers ?: currentSettings.showPhoneToOthers,
                allowStrangersMessage = allowStrangersMessage ?: currentSettings.allowStrangersMessage,
                notifyNewMessage = notifyNewMessage ?: currentSettings.notifyNewMessage,
                notifyTradeUpdate = notifyTradeUpdate ?: currentSettings.notifyTradeUpdate,
                notifySystemAnnouncement = notifySystemAnnouncement ?: currentSettings.notifySystemAnnouncement
            )
        )

        // 发送到服务器
        viewModelScope.launch {
            val request = UpdateSettingsRequest(
                showPhoneToOthers = showPhoneToOthers,
                allowStrangersMessage = allowStrangersMessage,
                notifyNewMessage = notifyNewMessage,
                notifyTradeUpdate = notifyTradeUpdate,
                notifySystemAnnouncement = notifySystemAnnouncement
            )
            when (val result = userRepository.updateSettings(request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(settings = result.data)
                }
                is Result.Error -> {
                    // 如果失败，回滚到之前的设置
                    loadSettings()
                }
                is Result.Loading -> {}
            }
        }
    }
}
