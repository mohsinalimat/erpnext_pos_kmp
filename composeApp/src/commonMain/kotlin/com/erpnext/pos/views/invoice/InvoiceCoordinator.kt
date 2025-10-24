package com.erpnext.pos.views.invoice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erpnext.pos.domain.models.PendingInvoiceBO
import org.koin.compose.viewmodel.koinViewModel

class InvoiceCoordinator(
    private val viewModel: InvoiceViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun onCustomerSelected(customerId: String) {
        viewModel.onSearchQueryChanged(customerId)
    }

    fun onDateSelected(date: String) {
        viewModel.onDateSelected(date)
    }

    fun onRefresh() {}
    fun onPrint() {}
    fun onItemClick(item: PendingInvoiceBO) {}
    fun isCashboxOpen() {}
    fun onClearSearch() {}
    fun fetchAll() {}
    fun getDetails(invoiceId: String) {}
    fun onError(error: String) {}
}

@Composable
fun rememberInvoiceCoordinator(): InvoiceCoordinator {
    val viewModel: InvoiceViewModel = koinViewModel()

    return remember(viewModel) {
        InvoiceCoordinator(
            viewModel = viewModel
        )
    }
}