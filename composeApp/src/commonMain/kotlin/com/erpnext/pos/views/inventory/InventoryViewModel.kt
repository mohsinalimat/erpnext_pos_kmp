@file:OptIn(FlowPreview::class)

package com.erpnext.pos.views.inventory

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.usecases.FetchCategoriesUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.domain.usecases.InventoryInput
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
    private val cashBoxManager: CashBoxManager,
    private val authStore: AuthInfoStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow = _stateFlow.asStateFlow()
    private var warehouseId: String? = ""
    private var priceList: String? = null

    private var basePagingData: PagingData<ItemBO> = PagingData.empty()
    private val searchFilter = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow("Todos los grupos de artículos")


    init {
        viewModelScope.launch {
            combine(searchFilter, categoryFilter) { query, category ->
                query to category
            }.debounce(350).collectLatest { (query, category) ->
                applyLocalFilter(query, category)
            }

            cashBoxManager.cashboxState.collectLatest { state ->
                when (state) {
                    is CashBoxState.Opened -> {
                        warehouseId = state.warehouse
                        priceList = state.priceList
                    }

                    else -> state
                }
            }
        }
    }

    fun fetchAllItems() {
        _stateFlow.update { InventoryState.Loading }
        executeUseCase(
            action = {
                val items = fetchInventoryItemUseCase.invoke(InventoryInput(warehouseId, priceList))
                    .cachedIn(viewModelScope)
                val categories = fetchCategoryUseCase.invoke(null)

                items.collectLatest { pagingData ->
                    basePagingData = pagingData
                    _stateFlow.update { InventoryState.Success(flowOf(pagingData), categories) }
                }
            },
            exceptionHandler = {
                _stateFlow.update { state -> InventoryState.Error(it.message ?: "Error") }
            }
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchFilter.value = query
    }

    fun onCategorySelected(category: String) {
        categoryFilter.value = category
    }

    private fun applyLocalFilter(query: String, category: String) {
        viewModelScope.launch {
            val filtered = basePagingData
                .filter { item ->
                    val matchesQuery = query.isBlank() ||
                            item.name.contains(query, ignoreCase = true) ||
                            item.itemCode.contains(query, ignoreCase = true)

                    val matchesCategory =
                        category == "Todos los grupos de artículos" ||
                                category.isBlank() ||
                                item.itemGroup.equals(category, ignoreCase = true)
                    matchesQuery && matchesCategory
                }

            val current = _stateFlow.value
            if (current is InventoryState.Success) {
                _stateFlow.value = InventoryState.Success(flowOf(filtered), current.categories)
            }
        }
    }

    suspend fun fetchBaseUrl(): String {
        return authStore.getCurrentSite() ?: ""
    }

    fun getItemDetail(itemId: String): ItemDto? {
        return null
    }

    fun refresh() {
        fetchAllItems()
    }

    fun onError(message: String) {
        print("InventoryViewModel onError = ${message}")
    }
}