package com.barter.app.ui.screens.main.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.TradeRequest
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradesUiState(
    val isLoading: Boolean = false,
    val receivedTrades: List<TradeRequest> = emptyList(),
    val sentTrades: List<TradeRequest> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TradesViewModel @Inject constructor(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradesUiState())
    val uiState: StateFlow<TradesUiState> = _uiState.asStateFlow()

    fun loadTrades() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // 加载收到的请求
            when (val received = tradeRepository.getReceivedTrades()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(receivedTrades = received.data.content)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = received.message)
                }
                is Result.Loading -> {}
            }

            // 加载发起的请求
            when (val sent = tradeRepository.getSentTrades()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sentTrades = sent.data.content
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = sent.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
}
