package com.erpnext.pos.views.inventory

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.FetchCategoriesUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.dto.ItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow = _stateFlow.asStateFlow()

    fun fetchAllItems() {
        _stateFlow.update { InventoryState.Loading }
        executeUseCase(
            action = {
                val items = fetchInventoryItemUseCase.invoke(null)
                    .cachedIn(viewModelScope)
                _stateFlow.update { InventoryState.Success(items) }
            },
            exceptionHandler = {
                _stateFlow.update { state -> InventoryState.Error(it.message ?: "Error") }
            }
        )
    }

    fun filter(category: String, query: String) {

    }

    fun getItemDetail(itemId: String): ItemDto? {
        return null
    }

    fun refresh() {
        fetchAllItems()
    }

    fun onError(message: String) {}
}