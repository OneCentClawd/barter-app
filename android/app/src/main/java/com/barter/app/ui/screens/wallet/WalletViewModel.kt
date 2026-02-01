package com.barter.app.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.CreditInfo
import com.barter.app.data.model.WalletInfo
import com.barter.app.data.model.WalletTransaction
import com.barter.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val wallet: WalletInfo? = null,
    val credit: CreditInfo? = null,
    val recentTransactions: List<WalletTransaction> = emptyList(),
    val signInMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 并行加载钱包和信用分信息
                val walletResponse = apiService.getWallet()
                val creditResponse = apiService.getCreditInfo()
                val transactionsResponse = apiService.getWalletTransactions(0, 5)
                
                var newState = _uiState.value.copy(isLoading = false)
                
                if (walletResponse.isSuccessful && walletResponse.body()?.success == true) {
                    newState = newState.copy(wallet = walletResponse.body()?.data)
                }
                
                if (creditResponse.isSuccessful && creditResponse.body()?.success == true) {
                    newState = newState.copy(credit = creditResponse.body()?.data)
                }
                
                if (transactionsResponse.isSuccessful && transactionsResponse.body()?.success == true) {
                    newState = newState.copy(recentTransactions = transactionsResponse.body()?.data?.content ?: emptyList())
                }
                
                _uiState.value = newState
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
    
    fun signIn() {
        viewModelScope.launch {
            try {
                val response = apiService.signIn()
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        signInMessage = response.body()?.message ?: "签到成功"
                    )
                    // 刷新钱包数据
                    loadWalletData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        signInMessage = response.body()?.message ?: "签到失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    signInMessage = "签到失败: ${e.message}"
                )
            }
        }
    }
    
    fun clearSignInMessage() {
        _uiState.value = _uiState.value.copy(signInMessage = null)
    }
}
