package com.barter.app.ui.screens.item

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barter.app.data.model.ItemCondition
import com.barter.app.data.repository.ItemRepository
import com.barter.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateItemUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateItemUiState())
    val uiState: StateFlow<CreateItemUiState> = _uiState.asStateFlow()

    fun createItem(
        title: String,
        description: String?,
        category: String?,
        condition: ItemCondition,
        wantedItems: String?,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = itemRepository.createItem(
                title = title,
                description = description,
                category = category,
                condition = condition,
                wantedItems = wantedItems,
                imageUris = imageUris
            )) {
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
