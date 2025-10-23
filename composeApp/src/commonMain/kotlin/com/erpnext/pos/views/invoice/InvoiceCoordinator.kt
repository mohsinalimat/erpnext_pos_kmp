package com.erpnext.pos.views.invoice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.domain.models.PendingInvoiceBO
import org.koin.compose.viewmodel.koinViewModel

class InvoiceCoordinator(
    viewModel: InvoiceViewModel
) {
    val screenStateFlow = viewModel.stateFlow
    
    fun onCustomerSelected(customerId: String) {}
    fun onDateSelected(start: String, end: String) {}
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