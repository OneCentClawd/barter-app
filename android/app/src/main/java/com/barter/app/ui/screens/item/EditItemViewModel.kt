package com.barter.app.ui.screens.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.Item
import com.barter.app.data.model.ItemCondition
import com.barter.app.data.model.UpdateItemRequest
import com.barter.app.data.repository.ItemRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditItemUiState(
    val isLoadingItem: Boolean = false,
    val isLoading: Boolean = false,
    val item: Item? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingItem = true, error = null)

            when (val result = itemRepository.getItem(itemId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingItem = false,
                        item = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingItem = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun updateItem(
        itemId: Long,
        title: String,
        description: String,
        category: String,
        condition: ItemCondition,
        wantedItems: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val request = UpdateItemRequest(
                title = title.takeIf { it.isNotBlank() },
                description = description.takeIf { it.isNotBlank() },
                category = category.takeIf { it.isNotBlank() },
                condition = condition,
                wantedItems = wantedItems.takeIf { it.isNotBlank() },
                status = null
            )

            when (val result = itemRepository.updateItem(itemId, request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
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
