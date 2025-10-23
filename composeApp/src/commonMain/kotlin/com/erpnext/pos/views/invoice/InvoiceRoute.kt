package com.erpnext.pos.views.invoice

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceRoute(
    coordinator: InvoiceCoordinator = rememberInvoiceCoordinator()
) {
    val uiState by coordinator.screenStateFlow.collectAsState(InvoiceState.Loading)
    val actions = rememberInvoiceActions(coordinator)

    InvoiceListScreen(uiState, actions)
}

@Composable
fun rememberInvoiceActions(coordinator: InvoiceCoordinator): InvoiceAction {
    return remember(coordinator) {
        InvoiceAction(
            onCustomerSelected = coordinator::onCustomerSelected,
            fetchAll = coordinator::fetchAll,
            isCashboxOpen = coordinator::isCashboxOpen,
            onItemClick = coordinator::onItemClick,
            onRefresh = coordinator::onRefresh,
            onError = coordinator::onError,
            onPrint = coordinator::onPrint,
            getDetails = coordinator::getDetails,
            onClearSearch = coordinator::onClearSearch,
            onDateSelected = coordinator::onDateSelected
        )
    }
}