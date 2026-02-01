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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private var cooldownJob: Job? = null

    init {
        loadCachedEmails()
    }
    
    private fun loadCachedEmails() {
        viewModelScope.launch {
            val emails = authRepository.getCachedEmails()
            _uiState.value = _uiState.value.copy(cachedEmails = emails)
        }
    }

    fun loginWithPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.loginWithEmail(email, password)) {
                is Result.Success -> {
                    authRepository.cacheEmail(email)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun loginWithCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.loginWithCode(email, code)) {
                is Result.Success -> {
                    authRepository.cacheEmail(email)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun sendLoginCode(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingCode = true, error = null)

            when (val result = authRepository.sendLoginCode(email)) {
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
}
