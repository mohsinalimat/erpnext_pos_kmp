package com.erpnext.pos.views.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.CheckCustomerCreditUseCase
import com.erpnext.pos.domain.usecases.CustomerCreditInput
import com.erpnext.pos.domain.usecases.CustomerSearchInput
import com.erpnext.pos.domain.usecases.CustomerSearchUseCase
import com.erpnext.pos.domain.usecases.FetchCustomerDetailUseCase
import com.erpnext.pos.domain.usecases.FetchCustomersUseCase
import com.erpnext.pos.navigation.NavigationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

class CustomerViewModel(
    private val navManager: NavigationManager,
    private val fetchCustomersUseCase: FetchCustomersUseCase,
    private val checkCustomerCreditUseCase: CheckCustomerCreditUseCase,
    private val searchUseCase: CustomerSearchUseCase,
    private val fetchCustomerDetailUseCase: FetchCustomerDetailUseCase
) : BaseViewModel() {
    private val _stateFlow: MutableStateFlow<CustomerState> =
        MutableStateFlow(CustomerState.Loading)
    val stateFlow = _stateFlow

    private var searchFilter by mutableStateOf("")
    private var selectedTerritory by mutableStateOf<String?>(null)

    init {
        fetchAllCustomers()
    }

    fun fetchAllCustomers() {
        executeUseCase(
            action = {
                fetchCustomersUseCase.invoke(null)
                // Llama searchUseCase para todos (sin filtro) y actualiza estado
                searchUseCase.invoke(CustomerSearchInput("", null)).collectLatest { customers ->
                    val territories = customers.map { it.territory }.distinct()
                    _stateFlow.value = CustomerState.Success(customers, territories)
                }
            },
            exceptionHandler = { _stateFlow.value = CustomerState.Error(it.message ?: "Error") }
        )
    }

    fun onSearchQueryChanged(query: String) {
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
                // Llama searchUseCase con filtros actuales
                searchUseCase.invoke(CustomerSearchInput(searchFilter, selectedTerritory))
                    .collectLatest { customers ->
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
        fetchAllCustomers()
    }
}