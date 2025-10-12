package com.erpnext.pos.views.inventory

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.FetchCategoriesUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.domain.usecases.InventoryInput
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
    private val cashBoxManager: CashBoxManager
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow = _stateFlow.asStateFlow()
    private var warehouseId: String? = ""
    private var priceList: String? = null

    init {
        viewModelScope.launch {
            cashBoxManager.cashboxState.collectLatest { state ->
                warehouseId = (state as? CashBoxState.Opened)?.warehouse
                priceList = (state as? CashBoxState.Opened)?.priceList
            }
        }
    }


    fun fetchAllItems() {
        _stateFlow.update { InventoryState.Loading }
        executeUseCase(
            action = {
                val items = fetchInventoryItemUseCase.invoke(InventoryInput(warehouseId, priceList))
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