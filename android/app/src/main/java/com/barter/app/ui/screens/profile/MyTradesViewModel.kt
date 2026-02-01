package com.barter.app.ui.screens.profile

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

data class MyTradesUiState(
    val isLoading: Boolean = false,
    val trades: List<TradeRequest> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MyTradesViewModel @Inject constructor(
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyTradesUiState())
    val uiState: StateFlow<MyTradesUiState> = _uiState.asStateFlow()

    fun loadMyTrades() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = tradeRepository.getMyTrades(0, 100)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trades = result.data.content
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
}
