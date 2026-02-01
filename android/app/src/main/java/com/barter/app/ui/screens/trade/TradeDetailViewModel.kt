package com.barter.app.ui.screens.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.ShipRequest
import com.barter.app.data.model.TradeMode
import com.barter.app.data.model.TradeStatus
import com.barter.app.data.remote.ApiService
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradeDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val status: TradeStatus? = null,
    val message: String? = null,
    val targetItemId: Long? = null,
    val targetItemTitle: String? = null,
    val targetItemImage: String? = null,
    val targetOwnerName: String? = null,
    val offeredItemId: Long? = null,
    val offeredItemTitle: String? = null,
    val offeredItemImage: String? = null,
    val offeredOwnerName: String? = null,
    val canRespond: Boolean = false,
    val isRequester: Boolean = false,
    val requesterConfirmed: Boolean = false,
    val targetConfirmed: Boolean = false,
    val myConfirmed: Boolean = false,
    // 远程交易
    val tradeMode: TradeMode? = null,
    val estimatedValue: Double? = null,
    val requesterTrackingNo: String? = null,
    val targetTrackingNo: String? = null,
    val requesterDepositPaid: Boolean = false,
    val targetDepositPaid: Boolean = false,
    val myDepositPaid: Boolean = false,
    val myTrackingNo: String? = null,
    val otherTrackingNo: String? = null,
    // 操作状态
    val isActioning: Boolean = false,
    val isActionSuccess: Boolean = false,
    val actionError: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class TradeDetailViewModel @Inject constructor(
    private val tradeRepository: TradeRepository,
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeDetailUiState())
    val uiState: StateFlow<TradeDetailUiState> = _uiState.asStateFlow()

    private var tradeId: Long = 0

    fun loadTrade(tradeId: Long) {
        this.tradeId = tradeId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val currentUserId = tokenManager.userId.first() ?: 0
            
            when (val result = tradeRepository.getTrade(tradeId)) {
                is Result.Success -> {
                    val trade = result.data
                    val canRespond = trade.targetItem?.owner?.id == currentUserId
                    val isRequester = trade.requester.id == currentUserId
                    val myConfirmed = if (isRequester) trade.requesterConfirmed else trade.targetConfirmed
                    
                    // 远程交易相关
                    val myDepositPaid = if (isRequester) trade.requesterDepositPaid else trade.targetDepositPaid
                    val myTrackingNo = if (isRequester) trade.requesterTrackingNo else trade.targetTrackingNo
                    val otherTrackingNo = if (isRequester) trade.targetTrackingNo else trade.requesterTrackingNo
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        status = trade.status,
                        message = trade.message,
                        targetItemId = trade.targetItem?.id,
                        targetItemTitle = trade.targetItem?.title,
                        targetItemImage = trade.targetItem?.coverImage,
                        targetOwnerName = trade.targetItem?.owner?.nickname ?: trade.targetItem?.owner?.username,
                        offeredItemId = trade.offeredItem?.id,
                        offeredItemTitle = trade.offeredItem?.title,
                        offeredItemImage = trade.offeredItem?.coverImage,
                        offeredOwnerName = trade.offeredItem?.owner?.nickname ?: trade.offeredItem?.owner?.username,
                        canRespond = canRespond,
                        isRequester = isRequester,
                        requesterConfirmed = trade.requesterConfirmed,
                        targetConfirmed = trade.targetConfirmed,
                        myConfirmed = myConfirmed,
                        tradeMode = trade.tradeMode,
                        estimatedValue = trade.estimatedValue,
                        requesterTrackingNo = trade.requesterTrackingNo,
                        targetTrackingNo = trade.targetTrackingNo,
                        requesterDepositPaid = trade.requesterDepositPaid,
                        targetDepositPaid = trade.targetDepositPaid,
                        myDepositPaid = myDepositPaid,
                        myTrackingNo = myTrackingNo,
                        otherTrackingNo = otherTrackingNo
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

    fun acceptTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            when (val result = tradeRepository.updateTradeStatus(tradeId, TradeStatus.ACCEPTED)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        isActionSuccess = true,
                        actionMessage = "已接受交换请求"
                    )
                    loadTrade(tradeId)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun rejectTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            when (val result = tradeRepository.updateTradeStatus(tradeId, TradeStatus.REJECTED)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        isActionSuccess = true,
                        actionMessage = "已拒绝交换请求"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun completeTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            when (val result = tradeRepository.updateTradeStatus(tradeId, TradeStatus.COMPLETED)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        isActionSuccess = true,
                        actionMessage = "已确认完成"
                    )
                    loadTrade(tradeId)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun cancelTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            when (val result = tradeRepository.updateTradeStatus(tradeId, TradeStatus.CANCELLED)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        isActionSuccess = true,
                        actionMessage = "已取消交换"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    /**
     * 支付保证金
     */
    fun payDeposit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            try {
                val response = apiService.payDeposit(tradeId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionMessage = "保证金支付成功"
                    )
                    loadTrade(tradeId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = response.body()?.message ?: "支付失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActioning = false,
                    actionError = e.message ?: "支付失败"
                )
            }
        }
    }
    
    /**
     * 发货
     */
    fun shipItem(trackingNo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            try {
                val response = apiService.shipItem(tradeId, ShipRequest(trackingNo))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionMessage = "发货成功"
                    )
                    loadTrade(tradeId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        actionError = response.body()?.message ?: "发货失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActioning = false,
                    actionError = e.message ?: "发货失败"
                )
            }
        }
    }
    
    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null, actionError = null)
    }
}
