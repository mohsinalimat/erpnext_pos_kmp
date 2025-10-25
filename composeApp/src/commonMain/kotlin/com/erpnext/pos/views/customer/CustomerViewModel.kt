package com.erpnext.pos.views.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.CheckCustomerCreditUseCase
import com.erpnext.pos.domain.usecases.CustomerCreditInput
import com.erpnext.pos.domain.usecases.CustomerFilterInput
import com.erpnext.pos.domain.usecases.FetchCustomerDetailUseCase
import com.erpnext.pos.domain.usecases.FetchCustomersUseCase
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.views.CashBoxManager
import com.erpnext.pos.views.CashBoxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val navManager: NavigationManager,
    private val cashboxManager: CashBoxManager,
    private val fetchCustomersUseCase: FetchCustomersUseCase,
    private val checkCustomerCreditUseCase: CheckCustomerCreditUseCase,
    private val fetchCustomerDetailUseCase: FetchCustomerDetailUseCase
) : BaseViewModel() {
    private val _stateFlow: MutableStateFlow<CustomerState> =
        MutableStateFlow(CustomerState.Loading)
    val stateFlow = _stateFlow

    private var territory by mutableStateOf<String?>(null)
    private var searchFilter by mutableStateOf<String?>(null)
    private var selectedTerritory by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            cashboxManager.observeCashBoxState().collectLatest { state ->
                when (state) {
                    is CashBoxState.Opened -> {
                        territory = state.territory
                        fetchAllCustomers(searchFilter, territory)
                    }

                    else -> null
                }
            }
        }
    }

    fun fetchAllCustomers(query: String? = null, territory: String? = null) {
        executeUseCase(
            action = {
                val input = CustomerFilterInput(query, territory)
                fetchCustomersUseCase.invoke(input).collectLatest { customers ->
                    val territories = customers.map { it.territory }.distinct()
                    _stateFlow.value = CustomerState.Success(customers, territories)
                }
            },
            exceptionHandler = { _stateFlow.value = CustomerState.Error(it.message ?: "Error") }
        )
    }

    fun onSearchQueryChanged(query: String?) {
        searchFilter = query
        updateFilteredCustomers()
    }

    fun onTerritorySelected(territory: String?) {
        selectedTerritory = territory
        updateFilteredCustomers()
    }

    private fun updateFilteredCustomers() {
        executeUseCase(
            action = {
                val searchFilterInput = CustomerFilterInput(searchFilter, selectedTerritory)
                fetchCustomersUseCase.invoke(searchFilterInput).collectLatest { customers ->
                    _stateFlow.value = CustomerState.Success(customers)
                }
            },
            exceptionHandler = { _stateFlow.value = CustomerState.Error(it.message ?: "Error") }
        )
    }

    fun checkCredit(customerId: String, amount: Double, onResult: (Boolean, String) -> Unit) {
        executeUseCase(
            action = {
                val isValid =
                    checkCustomerCreditUseCase.invoke(CustomerCreditInput(customerId, amount))
                val customer = fetchCustomerDetailUseCase.invoke(customerId)
                val message = if (isValid) {
                    "Crédito suficiente"
                } else {
                    "Crédito insuficiente: Disponible ${customer?.availableCredit ?: 0.0}"
                }
                onResult(isValid, message)
            },
            exceptionHandler = { onResult(false, it.message ?: "Error") }
        )
    }

    fun toDetails(customerId: String) {
        // navManager.navigateTo("customerDetails/$customerId")
    }

    fun onError(message: String) {
        _stateFlow.value = CustomerState.Error(message)
    }

    fun onClearSearch() {}

    fun onRefresh() {
        fetchAllCustomers(searchFilter, territory)
    }
}