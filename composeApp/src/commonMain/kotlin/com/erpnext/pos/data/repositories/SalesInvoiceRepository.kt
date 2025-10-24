package com.erpnext.pos.data.repositories

import androidx.paging.PagingData
import com.erpnext.pos.data.mappers.toPagingBO
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.domain.repositories.ISaleInvoiceRepository
import com.erpnext.pos.domain.usecases.PendingInvoiceInput
import com.erpnext.pos.remoteSource.datasources.InvoiceRemoteSource
import kotlinx.coroutines.flow.Flow

class SalesInvoiceRepository(
    private val source: InvoiceRemoteSource
) : ISaleInvoiceRepository {
    override suspend fun getPendingInvoices(info: PendingInvoiceInput): Flow<PagingData<PendingInvoiceBO>> {
        return source.getAllInvoices(info.pos, info.query, info.date).toPagingBO()
    }

    override suspend fun getInvoiceDetail(invoiceId: String): PendingInvoiceBO {
        TODO("Not yet implemented")
    }
}