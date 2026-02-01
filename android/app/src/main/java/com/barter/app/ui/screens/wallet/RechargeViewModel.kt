package com.barter.app.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RechargeUiState(
    val isLoading: Boolean = false,
    val currentBalance: Double = 0.0,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RechargeUiState())
    val uiState: StateFlow<RechargeUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }
    
    private fun loadBalance() {
        viewModelScope.launch {
            try {
                val response = apiService.getWallet()
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        currentBalance = response.body()?.data?.balance ?: 0.0
                    )
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
    
    fun recharge(amount: Double, paymentMethod: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // TODO: 实际支付流程
                // 1. 调用后端创建支付订单
                // 2. 拉起支付宝/微信支付
                // 3. 支付成功后回调确认
                
                // 目前模拟：直接调用充值接口（测试用）
                val response = apiService.recharge(
                    mapOf("amount" to amount, "paymentMethod" to paymentMethod)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "充值失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "充值失败: ${e.message}"
                )
            }
        }
    }
}
