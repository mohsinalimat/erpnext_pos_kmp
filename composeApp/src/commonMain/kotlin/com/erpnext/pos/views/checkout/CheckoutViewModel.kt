package com.erpnext.pos.views.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.usecases.CustomerFilterInput
import com.erpnext.pos.domain.usecases.FetchCustomersUseCase
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    val customersUseCase: FetchCustomersUseCase,
    val itemsUseCase: FetchInventoryItemUseCase,
    val cashboxManager: CashBoxManager
) : BaseViewModel() {

    private val _state: MutableStateFlow<CheckoutState> =
        MutableStateFlow(CheckoutState.Loading)
    val state = _state.asStateFlow()

    var territory by mutableStateOf<String?>(null)

    var customers by mutableStateOf<List<CustomerBO>>(emptyList())
    var selectedCustomer by mutableStateOf<CustomerBO?>(null)
    var isLoadingCustomers by mutableStateOf(false)

    var products by mutableStateOf<List<ItemBO>>(emptyList())
    var isLoadingItems by mutableStateOf(false)

    var subtotal by mutableStateOf(0.0)
    var tax by mutableStateOf(0.0)
    var discount by mutableStateOf(0.0)

    val total: Double
        get() = subtotal + tax - discount

    var initJob: Job? = null

    init {
        initJob?.cancel()
        initJob = viewModelScope.launch {
            cashboxManager.observeCashBoxState().collectLatest { state ->
                when (state) {
                    is CashBoxState.Opened -> {
                        territory = state.territory
                        loadCustomers(territory)
                    }

                    else -> null
                }
            }
        }
    }

    fun loadCustomers(territory: String?) {
        executeUseCase(
            action = {
                customersUseCase.invoke(CustomerFilterInput(null, territory))
                    .collectLatest { data ->
                        customers = data
                    }
            },
            exceptionHandler = { e ->
                _state.update { CheckoutState.Error(e.message ?: "") }
            },
            finallyHandler = { isLoadingCustomers = false }
        )
    }

    fun getAllProducts() {}

}