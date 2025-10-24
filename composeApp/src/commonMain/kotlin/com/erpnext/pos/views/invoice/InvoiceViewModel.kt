package com.erpnext.pos.views.invoice

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.domain.usecases.FetchPendingInvoiceUseCase
import com.erpnext.pos.domain.usecases.PendingInvoiceInput
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import com.erpnext.pos.views.inventory.InventoryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf

@OptIn(FlowPreview::class)
class InvoiceViewModel(
    private val fetchPendingInvoiceUseCase: FetchPendingInvoiceUseCase,
    private val cashboxManager: CashBoxManager, private val authStore: AuthInfoStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<InvoiceState> = MutableStateFlow(InvoiceState.Loading)
    val stateFlow: StateFlow<InvoiceState> = _stateFlow.asStateFlow()

    private var posProfileName: String = ""
    private var basePagingData: PagingData<PendingInvoiceBO> = PagingData.empty()
    private var itemsGlobal: Flow<PagingData<PendingInvoiceBO>>? = null

    private val searchFilter = MutableStateFlow("")
    private val dateFilter = MutableStateFlow<String?>(null)

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(searchFilter, dateFilter) { q, d -> q to d }
                .debounce(300)
                .collectLatest { (q, d) ->
                    applyLocalFilter(q, d)
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            var initialized = false
            cashboxManager.cashboxState.collectLatest { state ->
                println("InventoryViewModel - cashbox state = $state (initialized=$initialized)")
                when (state) {
                    is CashBoxState.Opened -> {
                        initialized = true
                        if (posProfileName != state.posProfileName) {
                            posProfileName = state.posProfileName
                            // for initial open we want force
                            fetchAll(force = true)
                        }
                    }

                    is CashBoxState.Closed -> {
                        if (initialized) {
                            posProfileName = ""
                            _stateFlow.update { InvoiceState.Empty }
                        } else {
                            println("InvoiceViewModel - ignoring initial Closed state")
                        }
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = runCatching { cashboxManager.cashboxState.value }.getOrNull()
                println("InventoryViewModel - initial cashboxState = $current")
                if (current is CashBoxState.Opened) {
                    posProfileName = current.posProfileName
                    fetchAll(force = true)
                } else {
                    // retry breve: permite que el manager emita si está inicializándose
                    delay(1200)
                    val retry = runCatching { cashboxManager.cashboxState.value }.getOrNull()
                    if (retry is CashBoxState.Opened) {
                        posProfileName = retry.posProfileName
                        fetchAll(force = true)
                    } else {
                        // No hay caja abierta - publíca Empty para UX estable
                        _stateFlow.update { InvoiceState.Empty }
                    }
                }
            } catch (e: Exception) {
                println("InvoiceViewModel - error reading initial cashbox state: ${e.message}")
                _stateFlow.update { InvoiceState.Empty }
            }
        }
    }

    fun fetchAll(force: Boolean = false) {
        if (!force && fetchJob?.isActive == true) {
            return
        }

        _stateFlow.update { InvoiceState.Loading }

        fetchJob?.cancel()
        fetchJob = executeUseCase(action = {
            val input = PendingInvoiceInput(posProfileName)
            val invoices = fetchPendingInvoiceUseCase.invoke(input)
            itemsGlobal = invoices

            _stateFlow.update { InvoiceState.Success(invoices) }

            launch {
                invoices.collectLatest { pagingData ->
                    basePagingData = pagingData

                    val currentQuery = searchFilter.value

                    if (currentQuery.isBlank()) {
                        _stateFlow.update { InvoiceState.Success(invoices) }
                    } else {
                        val filtered = pagingData.filter { item ->
                            if (currentQuery.isBlank()) return@filter true
                            val q = currentQuery.lowercase()
                            (item.customer?.lowercase()
                                ?.contains(q) == true || item.customerPhone?.lowercase()
                                ?.contains(q) == true)
                        }
                        _stateFlow.update {
                            InvoiceState.Success(flowOf(filtered))
                        }
                    }
                }
            }
        }, exceptionHandler = { e ->
            _stateFlow.update {
                InvoiceState.Error(
                    e.message ?: "Error cargando facturas pendientes"
                )
            }
        }, finallyHandler = { })
    }

    fun onSearchQueryChanged(value: String) {
        searchFilter.value = value
    }

    fun onDateSelected(value: String) {
        dateFilter.value = value
    }

    private fun applyLocalFilter(query: String, date: String?) {
        fetchJob?.cancel()
        fetchJob = executeUseCase(action = {
            val input = PendingInvoiceInput(posProfileName, query, date)
            fetchPendingInvoiceUseCase.invoke(input).collectLatest { pagingData ->
                _stateFlow.value = InvoiceState.Success(flowOf(pagingData))
            }
        }, exceptionHandler = { e ->
            _stateFlow.update { InvoiceState.Error(e.message ?: "") }
        })
    }
}