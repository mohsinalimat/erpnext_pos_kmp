package com.erpnext.pos.domain.repositories

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.domain.usecases.PendingInvoiceInput
import kotlinx.coroutines.flow.Flow

interface ISaleInvoiceRepository {
    suspend fun getPendingInvoices(info: PendingInvoiceInput): Flow<PagingData<PendingInvoiceBO>>
    suspend fun getInvoiceDetail(invoiceId: String): PendingInvoiceBO
}