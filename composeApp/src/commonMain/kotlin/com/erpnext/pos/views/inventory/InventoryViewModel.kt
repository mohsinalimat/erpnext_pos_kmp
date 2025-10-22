@file:OptIn(FlowPreview::class)

package com.erpnext.pos.views.inventory

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.models.CategoryBO
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.firstOrNull

class InventoryViewModel(
    private val navManager: NavigationManager,
    private val fetchCategoryUseCase: FetchCategoriesUseCase,
    private val fetchInventoryItemUseCase: FetchInventoryItemUseCase,
    private val cashboxManager: CashBoxManager,
    private val authStore: AuthInfoStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InventoryState> =
        MutableStateFlow(InventoryState.Loading)
    val stateFlow = _stateFlow.asStateFlow()

    private var warehouseId: String? = null
    private var priceList: String? = null

    // Último PagingData emitido (para filtrado cliente)
    private var basePagingData: PagingData<ItemBO> = PagingData.empty<ItemBO>()

    // Flow global original proveniente del Pager (mantiene RemoteMediator activo)
    private var itemsFlowGlobal: kotlinx.coroutines.flow.Flow<PagingData<ItemBO>>? = null

    private val searchFilter = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow("Todos los grupos de artículos")

    // Job que representa la tarea de fetch actual (evita reentradas)
    private var fetchJob: Job? = null

    init {
        // 1) collector para filtros (dedicado)
        viewModelScope.launch {
            combine(searchFilter, categoryFilter) { query, category ->
                query to category
            }.debounce(350)
                .collectLatest { (query, category) ->
                    println("InventoryViewModel - filter changed: query='$query' category='$category'")
                    applyLocalFilter(query, category)
                }
        }

        // 2) collector separado para cashboxState (IO) — sin dropWhile, con flag initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Leer valor actual directamente
                val current = cashboxManager.cashboxState.value
                println("InventoryViewModel - initial cashboxState = $current")

                if (current is CashBoxState.Opened) {
                    warehouseId = current.warehouse
                    priceList = current.priceList
                    fetchAllItems()
                } else {
                    // Esperar un breve momento a que se emita la primera caja abierta
                    kotlinx.coroutines.delay(1500)
                    val retry = cashboxManager.cashboxState.value
                    if (retry is CashBoxState.Opened) {
                        warehouseId = retry.warehouse
                        priceList = retry.priceList
                        fetchAllItems()
                    } else {
                        _stateFlow.update { InventoryState.Empty }
                    }
                }
            } catch (e: Exception) {
                println("InventoryViewModel - error reading initial cashbox state: ${e.message}")
                _stateFlow.update { InventoryState.Empty }
            }
        }

        // 3) intento proactivo de inicializar si ya hay cashbox abierto (no bloqueante)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = try {
                    cashboxManager.cashboxState.firstOrNull()
                } catch (e: Exception) {
                    null
                }
                if (current is CashBoxState.Opened) {
                    // si ya hay uno abierto, inicializamos inmediatamente
                    warehouseId = current.warehouse
                    priceList = current.priceList
                    fetchAllItems()
                } else {
                    // si no hay caja abierta en inicialización, mostramos Empty para UX estable
                    _stateFlow.update { InventoryState.Empty }
                }
            } catch (e: Exception) {
                println("InventoryViewModel - error reading initial cashbox state: ${e.message}")
            }
        }
    }

    fun fetchAllItems() {
        // Si ya hay un fetch en curso, no lanzar otro
        if (fetchJob?.isActive == true) {
            println("InventoryViewModel - fetch already in progress, skipping")
            return
        }

        // indica carga inicial (shimmer) mientras se lanza el flow
        _stateFlow.update { InventoryState.Loading }

        // lanzamos el trabajo de fetch en viewModelScope y lo guardamos en fetchJob
        fetchJob = viewModelScope.launch {
            try {
                // Obtener flow de paging (no lo collectamos aquí directamente)
                val itemsFlow =
                    fetchInventoryItemUseCase.invoke(InventoryInput(warehouseId, priceList))
                        .cachedIn(this) // cached in this coroutine scope for lifecycle control

                // Guardamos el flow global para poder reapuntar cuando no haya filtros
                itemsFlowGlobal = itemsFlow

                // Obtener categorías en paralelo sin bloquear
                val categoriesDeferred = async(Dispatchers.IO) {
                    try {
                        fetchCategoryUseCase.invoke(null)
                    } catch (e: Exception) {
                        println("InventoryViewModel - error fetching categories: ${e.message}")
                        emptyList()
                    }
                }

                // Publica el flow global de inmediato para que UI lo consuma y Paging dispare RemoteMediator
                val provisionalCategories = try {
                    categoriesDeferred.await()
                } catch (e: Exception) {
                    emptyList()
                }

                // IMPORTANT: publish the global flow so Compose collects and triggers paging/mediator
                /*_stateFlow.update {
                    InventoryState.Success(itemsFlow, provisionalCategories)
                }*/

                // Ahora lanzamos la recolección del flujo paginado en un hijo separado (no bloqueará el body)
                launch {
                    itemsFlow.collectLatest { pagingData ->
                        println("InventoryViewModel - received pagingData emission")
                        basePagingData = pagingData
                        // Si hay filtros activos, aplica filtro cliente y expone ese flow filtrado
                        val currentQuery = searchFilter.value
                        val currentCategory = categoryFilter.value
                        if (currentQuery.isBlank() && (currentCategory == "Todos los grupos de artículos" || currentCategory == "Todos" ||  currentCategory.isBlank())) {
                            // mantenemos el flow global (ya está publicado), pero actualizamos basePagingData
                            _stateFlow.update {
                                InventoryState.Success(
                                    itemsFlow,
                                    provisionalCategories
                                )
                            }
                        } else {
                            val filtered = pagingData.filter { item ->
                                val matchesCategory =
                                    (currentCategory == "Todos los grupos de artículos" || currentCategory.isBlank())
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
                                    provisionalCategories
                                )
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                println("InventoryViewModel - fetchAllItems error: ${e.message}")
                _stateFlow.update { InventoryState.Error(e.message ?: "Error cargando inventario") }
            } finally {
                println("InventoryViewModel - fetchAllItems finished/clean (collectors may still run)")
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
            // Si no hay filtro, reapuntar al flow global para mantener RemoteMediator vivo
            if (query.isBlank() && (category == "Todos los grupos de artículos" || category.isBlank())) {
                itemsFlowGlobal?.let { flow ->
                    val currentCategories =
                        (_stateFlow.value as? InventoryState.Success)?.categories ?: emptyList()
                    _stateFlow.value = InventoryState.Success(flow, currentCategories)
                    return@launch
                }
            }

            // Si hay filtro activo, aplica filtrado local sobre el último PagingData conocido
            val filtered = basePagingData.filter { item ->
                val matchesCategory =
                    (category == "Todos los grupos de artículos" || category.isBlank())
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

    fun getItemDetail(itemId: String): ItemDto? {
        return null
    }

    fun refresh() {
        // Cancela fetch anterior (si existe) y vuelve a lanzar
        viewModelScope.launch {
            try {
                fetchJob?.cancelAndJoin()
            } catch (e: Exception) {
                println("InventoryViewModel - error cancelling previous fetch: ${e.message}")
            } finally {
                fetchJob = null
                fetchAllItems()
            }
        }
    }

    fun onError(message: String) {
        print("InventoryViewModel onError = $message")
    }
}
