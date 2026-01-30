package com.barter.app.ui.screens.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.local.TokenManager
import com.barter.app.data.model.TradeStatus
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
    val targetItemTitle: String? = null,
    val targetItemImage: String? = null,
    val targetOwnerName: String? = null,
    val offeredItemTitle: String? = null,
    val offeredItemImage: String? = null,
    val offeredOwnerName: String? = null,
    val canRespond: Boolean = false,
    val isRequester: Boolean = false,
    val requesterConfirmed: Boolean = false,
    val targetConfirmed: Boolean = false,
    val myConfirmed: Boolean = false,  // 当前用户是否已确认
    val isActioning: Boolean = false,
    val isActionSuccess: Boolean = false,
    val actionError: String? = null
)

@HiltViewModel
class TradeDetailViewModel @Inject constructor(
    private val tradeRepository: TradeRepository,
    private val tokenManager: TokenManager
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
                    // 判断是否是收到的请求（对方是发起者）
                    val canRespond = trade.targetItem?.owner?.id == currentUserId
                    val isRequester = trade.requester.id == currentUserId
                    
                    // 判断当前用户是否已确认
                    val myConfirmed = if (isRequester) trade.requesterConfirmed else trade.targetConfirmed
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        status = trade.status,
                        message = trade.message,
                        targetItemTitle = trade.targetItem?.title,
                        targetItemImage = trade.targetItem?.coverImage,
                        targetOwnerName = trade.targetItem?.owner?.nickname ?: trade.targetItem?.owner?.username,
                        offeredItemTitle = trade.offeredItem?.title,
                        offeredItemImage = trade.offeredItem?.coverImage,
                        offeredOwnerName = trade.offeredItem?.owner?.nickname ?: trade.offeredItem?.owner?.username,
                        canRespond = canRespond,
                        isRequester = isRequester,
                        requesterConfirmed = trade.requesterConfirmed,
                        targetConfirmed = trade.targetConfirmed,
                        myConfirmed = myConfirmed
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
                        isActionSuccess = true
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

    fun rejectTrade() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActioning = true, actionError = null)
            
            when (val result = tradeRepository.updateTradeStatus(tradeId, TradeStatus.REJECTED)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActioning = false,
                        isActionSuccess = true
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
                        isActionSuccess = true
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
}
