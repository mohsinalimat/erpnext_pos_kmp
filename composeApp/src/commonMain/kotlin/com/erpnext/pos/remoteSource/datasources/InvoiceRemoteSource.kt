package com.erpnext.pos.remoteSource.datasources

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.erpnext.pos.localSource.dao.PendingInvoiceDao
import com.erpnext.pos.localSource.entities.SalesInvoiceEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.paging.InvoiceRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

@OptIn(ExperimentalPagingApi::class)
class InvoiceRemoteSource(
    private val apiService: APIService,
    private val invoiceDao: PendingInvoiceDao
) {
    fun getAllInvoices(
        posProfileName: String,
        query: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): Flow<PagingData<SalesInvoiceEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 4,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            remoteMediator = InvoiceRemoteMediator(apiService, invoiceDao, posProfileName),
            initialKey = null
        ) {
            invoiceDao.getFilteredInvoices(query, fromDate, toDate)
        }.flow.onStart {
            emit(PagingData.empty())
        }
    }
}