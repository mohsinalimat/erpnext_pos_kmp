package com.erpnext.pos.remoteSource.datasources

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.erpnext.pos.localSource.dao.PendingInvoiceDao
import com.erpnext.pos.localSource.dao.SalesInvoiceDao
import com.erpnext.pos.localSource.entities.PendingSalesInvoiceEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceWithItemsAndPayments
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.SalesInvoiceDto
import com.erpnext.pos.remoteSource.mapper.toDto
import com.erpnext.pos.remoteSource.mapper.toEntities
import com.erpnext.pos.remoteSource.paging.InvoiceRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

@OptIn(ExperimentalPagingApi::class)
class SalesInvoiceRemoteSource(
    private val apiService: APIService,
    private val pendingInvoiceDao: PendingInvoiceDao,
    private val salesInvoiceDao: SalesInvoiceDao,
) {

    /**
     * Obtener una factura específica desde ERPNext y guardarla localmente
     */
    suspend fun fetchInvoice(name: String) {
        val dto: SalesInvoiceDto = apiService.getSalesInvoiceByName(name)
        val entities = dto.toEntities()
        salesInvoiceDao.insertInvoice(entities.invoice)
        salesInvoiceDao.insertItems(entities.items)
        salesInvoiceDao.insertPayments(entities.payments)
    }

    /**
     * Crear factura en ERPNext y guardar localmente
     */
    suspend fun createInvoice(invoice: SalesInvoiceWithItemsAndPayments) {
        val dto = invoice.toDto()
        val createdDto = apiService.createSalesInvoice(dto)
        val entities = createdDto.toEntities()
        salesInvoiceDao.insertInvoice(entities.invoice)
        salesInvoiceDao.insertItems(entities.items)
        salesInvoiceDao.insertPayments(entities.payments)
    }

    /**
     * Actualizar factura remota y sincronizar local
     */
    suspend fun updateInvoice(invoice: SalesInvoiceWithItemsAndPayments) {
        val dto = invoice.toDto()
        val updatedDto =
            apiService.updateSalesInvoice(invoice.invoice.invoiceName ?: "", dto)
        val entities = updatedDto.toEntities()
        salesInvoiceDao.insertInvoice(entities.invoice)
        salesInvoiceDao.insertItems(entities.items)
        salesInvoiceDao.insertPayments(entities.payments)
    }

    fun getAllInvoices(
        posProfileName: String,
        query: String? = null,
        date: String? = null,
    ): Flow<PagingData<PendingSalesInvoiceEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 4,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            remoteMediator = InvoiceRemoteMediator(apiService, pendingInvoiceDao, posProfileName),
            initialKey = null
        ) {
            pendingInvoiceDao.getFilteredInvoices(query, date)
        }.flow.onStart {
            emit(PagingData.empty())
        }
    }
}