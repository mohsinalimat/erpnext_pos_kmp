@file:OptIn(FlowPreview::class)

package com.erpnext.pos.views.inventory

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.usecases.FetchCategoriesUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.domain.usecases.InventoryInput
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay

class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
    private val cashboxManager: CashBoxManager,
    private val authStore: AuthInfoStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow: StateFlow<InventoryState> = _stateFlow.asStateFlow()

    private var warehouseId: String? = null
    private var priceList: String? = null

    // último PagingData emitido (para filtrado local)
    private var basePagingData: PagingData<ItemBO> = PagingData.empty()

    // flow global original proveniente del Pager (mantiene RemoteMediator activo)
    private var itemsFlowGlobal: Flow<PagingData<ItemBO>>? = null

    private val searchFilter = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow("Todos los grupos de artículos")

    private var fetchJob: Job? = null

    init {
        // 1) Observador de filtros (dedicado)
        viewModelScope.launch {
            combine(searchFilter, categoryFilter) { q, c -> q to c }
                .debounce(300)
                .collectLatest { (q, c) ->
                    applyLocalFilter(q, c)
                }
        }

        // 2) Observador de cashbox: reacciona a cambios posteriores
        viewModelScope.launch(Dispatchers.IO) {
            var initialized = false
            cashboxManager.cashboxState.collectLatest { state ->
                println("InventoryViewModel - cashbox state = $state (initialized=$initialized)")
                when (state) {
                    is CashBoxState.Opened -> {
                        initialized = true
                        if (warehouseId != state.warehouse || priceList != state.priceList) {
                            warehouseId = state.warehouse
                            priceList = state.priceList
                            // for initial open we want force
                            fetchAllItems(force = true)
                        }
                    }

                    is CashBoxState.Closed -> {
                        if (initialized) {
                            warehouseId = null
                            priceList = null
                            _stateFlow.update { InventoryState.Empty }
                        } else {
                            println("InventoryViewModel - ignoring initial Closed state")
                        }
                    }

                    else -> Unit
                }
            }
        }

        // 3) Carga inicial garantizada: intenta leer valor actual; si no hay open intenta un retry corto
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = runCatching { cashboxManager.cashboxState.value }.getOrNull()
                println("InventoryViewModel - initial cashboxState = $current")
                if (current is CashBoxState.Opened) {
                    warehouseId = current.warehouse
                    priceList = current.priceList
                    fetchAllItems(force = true)
                } else {
                    // retry breve: permite que el manager emita si está inicializándose
                    delay(1200)
                    val retry = runCatching { cashboxManager.cashboxState.value }.getOrNull()
                    if (retry is CashBoxState.Opened) {
                        warehouseId = retry.warehouse
                        priceList = retry.priceList
                        fetchAllItems(force = true)
                    } else {
                        // No hay caja abierta - publíca Empty para UX estable
                        _stateFlow.update { InventoryState.Empty }
                    }
                }
            } catch (e: Exception) {
                println("InventoryViewModel - error reading initial cashbox state: ${e.message}")
                _stateFlow.update { InventoryState.Empty }
            }
        }
    }

    /**
     * Fetch principal. force = true ignora fetchJob activo (útil para apertura de caja).
     */
    fun fetchAllItems(force: Boolean = false) {
        if (!force && fetchJob?.isActive == true) {
            println("InventoryViewModel - fetch already in progress, skipping")
            return
        }

        // indicamos Loading inicialmente
        _stateFlow.update { InventoryState.Loading }

        // cancelamos trabajo anterior y lanzamos nuevo
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val itemsFlow =
                    fetchInventoryItemUseCase.invoke(InventoryInput(warehouseId, priceList))
                        .cachedIn(this)

                itemsFlowGlobal = itemsFlow

                // categorías paralelo (no crítico)
                val categoriesDeferred = async(Dispatchers.IO) {
                    runCatching { fetchCategoryUseCase.invoke(null) }.getOrElse { emptyList() }
                }

                val categories = categoriesDeferred.await()

                // publicamos el flow global para que Compose lo empiece a consumir (y Paging dispare RM)
                _stateFlow.update { InventoryState.Success(itemsFlow, categories) }

                // collector que mantiene basePagingData actualizado y aplica filtros si hay
                launch {
                    itemsFlow.collectLatest { pagingData ->
                        println("InventoryViewModel - received pagingData emission (size unknown)")
                        basePagingData = pagingData

                        val currentQuery = searchFilter.value
                        val currentCategory = categoryFilter.value

                        if (currentQuery.isBlank() && (currentCategory == "Todos los grupos de artículos" || currentCategory.isBlank() || currentCategory == "Todos")) {
                            // re-expose the global flow (keeps RM alive)
                            _stateFlow.update { InventoryState.Success(itemsFlow, categories) }
                        } else {
                            // produce filtered flow from cached PagingData
                            val filtered = pagingData.filter { item ->
                                val matchesCategory =
                                    (currentCategory == "Todos los grupos de artículos" || currentCategory.isBlank() || currentCategory == "Todos")
                                            || (item.itemGroup?.equals(
                                        currentCategory,
                                        ignoreCase = true
                                    ) == true)
                                if (!matchesCategory) return@filter false
                                if (currentQuery.isBlank()) return@filter true
                                val q = currentQuery.lowercase()
                                (item.name?.lowercase()?.contains(q) == true) ||
                                        (item.itemCode?.lowercase()?.contains(q) == true)
                            }
                            _stateFlow.update {
                                InventoryState.Success(
                                    flowOf(filtered),
                                    categories
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("InventoryViewModel - fetchAllItems error: ${e.message}")
                _stateFlow.update { InventoryState.Error(e.message ?: "Error cargando inventario") }
            } finally {
                println("InventoryViewModel - fetchAllItems finished (collectors may still run)")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchFilter.value = query
    }

    fun onCategorySelected(category: String) {
        categoryFilter.value = category
    }

    private fun applyLocalFilter(query: String, category: String) {
        viewModelScope.launch {
            // si no hay filtro, reapuntar al flowGlobal para mantener remote mediator
            if (query.isBlank() && (category == "Todos los grupos de artículos" || category.isBlank() || category == "Todos")) {
                itemsFlowGlobal?.let { flow ->
                    val currentCategories =
                        (_stateFlow.value as? InventoryState.Success)?.categories ?: emptyList()
                    _stateFlow.value = InventoryState.Success(flow, currentCategories)
                    return@launch
                }
            }

            // aplica filtro sobre último pagingData conocido
            val filtered = basePagingData.filter { item ->
                val matchesCategory =
                    (category == "Todos los grupos de artículos" || category.isBlank() || category == "Todos")
                            || (item.itemGroup?.equals(category, ignoreCase = true) == true)
                if (!matchesCategory) return@filter false
                if (query.isBlank()) return@filter true
                val q = query.lowercase()
                (item.name?.lowercase()?.contains(q) == true) ||
                        (item.itemCode?.lowercase()?.contains(q) == true)
            }

            val currentCategories =
                (_stateFlow.value as? InventoryState.Success)?.categories ?: emptyList()
            _stateFlow.value = InventoryState.Success(flowOf(filtered), currentCategories)
        }
    }

    suspend fun fetchBaseUrl(): String {
        return authStore.getCurrentSite() ?: ""
    }

    fun getItemDetail(itemId: String): ItemDto? = null

    fun refresh() {
        viewModelScope.launch {
            try {
                fetchJob?.cancelAndJoin()
            } catch (e: Exception) {
                println("InventoryViewModel - error cancelling previous fetch: ${e.message}")
            } finally {
                fetchJob = null
                fetchAllItems(force = true)
            }
        }
    }

    fun onError(message: String) {
        println("InventoryViewModel onError = $message")
    }
}
