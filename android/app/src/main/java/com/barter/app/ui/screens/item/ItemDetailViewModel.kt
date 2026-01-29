package com.barter.app.ui.screens.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.Item
import com.barter.app.data.repository.AuthRepository
import com.barter.app.data.repository.ItemRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val isOwner: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = itemRepository.getItem(itemId)) {
                is Result.Success -> {
                    val currentUserId = authRepository.getUserId().first()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        item = result.data,
                        isOwner = result.data.owner.id == currentUserId
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
