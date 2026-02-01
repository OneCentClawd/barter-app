package com.barter.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.repository.AuthRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    private var cooldownJob: Job? = null

    fun sendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingCode = true, error = null)

            when (val result = authRepository.sendVerificationCode(email)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false, 
                        codeSent = true,
                        cooldownSeconds = 60
                    )
                    startCooldown()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false, 
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            while (_uiState.value.cooldownSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    cooldownSeconds = _uiState.value.cooldownSeconds - 1
                )
            }
        }
    }

    fun register(
        username: String, 
        email: String, 
        password: String, 
        nickname: String,
        verificationCode: String,
        referrerId: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.register(
                username = username, 
                email = email, 
                password = password, 
                nickname = nickname,
                verificationCode = verificationCode,
                referrerId = referrerId
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
