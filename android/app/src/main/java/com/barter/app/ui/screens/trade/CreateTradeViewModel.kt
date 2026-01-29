package com.barter.app.ui.screens.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.Item
import com.barter.app.data.model.ItemListItem
import com.barter.app.data.repository.ItemRepository
import com.barter.app.data.repository.Result
import com.barter.app.data.repository.TradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTradeUiState(
    val isLoadingItems: Boolean = false,
    val isLoading: Boolean = false,
    val targetItem: Item? = null,
    val myItems: List<ItemListItem> = emptyList(),
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateTradeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val tradeRepository: TradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTradeUiState())
    val uiState: StateFlow<CreateTradeUiState> = _uiState.asStateFlow()

    fun loadData(targetItemId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingItems = true, error = null)

            // 加载目标物品
            when (val result = itemRepository.getItem(targetItemId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(targetItem = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is Result.Loading -> {}
            }

            // 加载我的可用物品
            when (val result = itemRepository.getMyAvailableItems()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingItems = false,
                        myItems = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingItems = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun createTrade(targetItemId: Long, offeredItemId: Long, message: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = tradeRepository.createTrade(targetItemId, offeredItemId, message)) {
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
