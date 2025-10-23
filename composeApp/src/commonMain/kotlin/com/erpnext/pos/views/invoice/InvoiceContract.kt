package com.erpnext.pos.views.invoice

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.PendingInvoiceBO
import kotlinx.coroutines.flow.Flow

sealed class InvoiceState {
    object Loading : InvoiceState()
    object Empty : InvoiceState()
    data class Success(val invoices: Flow<PagingData<PendingInvoiceBO>>) : InvoiceState()
    data class Error(val error: String) : InvoiceState()
}

data class InvoiceAction(
    val onCustomerSelected: (String) -> Unit = {},
    val onDateSelected: (String, String) -> Unit = { _, _ -> },
    val onRefresh: () -> Unit = {},
    val onPrint: () -> Unit = {},
    val onItemClick: (PendingInvoiceBO) -> Unit = {},
    val isCashboxOpen: () -> Unit = {},
    val onClearSearch: () -> Unit = {},
    val fetchAll: () -> Unit = { },
    val getDetails: (String) -> Unit = { },
    val onError: (error: String) -> Unit = {}
)