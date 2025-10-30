@file:OptIn(FlowPreview::class)

package com.erpnext.pos.views.inventory

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.base.Resource
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.usecases.FetchCategoriesUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.domain.usecases.InventoryInput
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay

@OptIn(FlowPreview::class)
class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
    private val cashboxManager: CashBoxManager,
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow: StateFlow<InventoryState> = _stateFlow.asStateFlow()

    private var warehouseId: String? = null
    private var priceList: String? = null

    private var basePagingData: PagingData<ItemBO> = PagingData.empty()
    private var itemsFlowGlobal: Flow<PagingData<ItemBO>>? = null

    private val searchFilter = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow("Todos los grupos de artículos")

    private var fetchJob: Job? = null
    private var categoriesJob: Job? = null

    init {
        observeFilters()
        observeCashbox()
        initialFetch()
    }

    /** Observa cambios de búsqueda y categoría */
    private fun observeFilters() {
        viewModelScope.launch {
            combine(searchFilter, categoryFilter) { q, c -> q to c }
                .debounce(300)
                .collectLatest { (q, c) -> applyLocalFilter(q, c) }
        }
    }

    /** Observa estado de caja */
    private fun observeCashbox() {
        viewModelScope.launch(Dispatchers.IO) {
            var initialized = false
            cashboxManager.cashboxState.collectLatest { state ->
                when (state) {
                    is CashBoxState.Opened -> {
                        initialized = true
                        if (warehouseId != state.warehouse) {
                            warehouseId = state.warehouse
                            fetchAllItems(force = true)
                        }
                    }

                    is CashBoxState.Closed -> {
                        if (initialized) {
                            warehouseId = null
                            priceList = null
                            _stateFlow.update { InventoryState.Empty }
                        }
                    }
                }
            }
        }
    }

    /** Carga inicial garantizada */
    private fun initialFetch() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = cashboxManager.cashboxState.value
                if (current is CashBoxState.Opened) {
                    warehouseId = current.warehouse
                    fetchAllItems(force = true)
                } else {
                    delay(1200)
                    val retry = cashboxManager.cashboxState.value
                    if (retry is CashBoxState.Opened) {
                        warehouseId = retry.warehouse
                        fetchAllItems(force = true)
                    } else {
                        _stateFlow.update { InventoryState.Empty }
                    }
                }
            } catch (_: Exception) {
                _stateFlow.update { InventoryState.Empty }
            }
        }
    }

    /** Fetch principal */
    fun fetchAllItems(force: Boolean = false) {
        if (!force && fetchJob?.isActive == true) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            _stateFlow.update { InventoryState.Loading }

            // 1️⃣ Fetch inventario Paging
            val itemsFlow = fetchInventoryItemUseCase.invoke(
                InventoryInput(warehouseId, priceList)
            ).cachedIn(this)
            itemsFlowGlobal = itemsFlow

            // Collector que mantiene basePagingData actualizado
            launch {
                itemsFlow.collectLatest { pagingData -> basePagingData = pagingData }
            }

            // 2️⃣ Fetch categorías (NBR)
            categoriesJob?.cancel()
            categoriesJob = launch(Dispatchers.IO) {
                fetchCategoryUseCase.invoke(null).collectLatest { resource ->
                    val categories = when (resource) {
                        is Resource.Success -> resource.data
                        else -> emptyList()
                    }

                    // 3️⃣ Exponer estado a UI
                    _stateFlow.update {
                        InventoryState.Success(itemsFlow, categories)
                    }

                    // En caso de error, notificar
                    if (resource is Resource.Error) onError(resource.message)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchFilter.value = query
    }

    fun onCategorySelected(category: String) {
        categoryFilter.value = category
    }

    fun getItemDetail(id: String) {}

    /** Aplica filtro sobre último PagingData emitido */
    private fun applyLocalFilter(query: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) { // <<== usar Default o IO
            val filtered = basePagingData.filter { item ->
                val matchesCategory = category == "Todos" || category.isBlank() || item.itemGroup.equals(category, true)
                val matchesQuery = query.isBlank() || item.name.contains(query, true) || item.itemCode.contains(query, true)
                matchesCategory && matchesQuery
            }

            val currentCategories = (_stateFlow.value as? InventoryState.Success)?.categories ?: emptyList()
            _stateFlow.value = InventoryState.Success(flowOf(filtered), currentCategories)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                fetchJob?.cancelAndJoin()
            } catch (_: Exception) {
            }
            fetchJob = null
            fetchAllItems(force = true)
        }
    }

    fun onError(message: String?) {
        println("InventoryViewModel onError = $message")
    }
}
