package com.erpnext.pos.data.repositories

import androidx.paging.PagingData
import com.erpnext.pos.data.mappers.toEntities
import com.erpnext.pos.data.mappers.toDto
import com.erpnext.pos.data.mappers.toPagingBO
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.domain.repositories.ISaleInvoiceRepository
import com.erpnext.pos.domain.usecases.PendingInvoiceInput
import com.erpnext.pos.localSource.dao.SalesInvoiceDao
import com.erpnext.pos.localSource.entities.*
import com.erpnext.pos.remoteSource.datasources.SalesInvoiceRemoteSource
import com.erpnext.pos.remoteSource.dto.SalesInvoiceDto
import com.erpnext.pos.remoteSource.mapper.toEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SalesInvoiceRepository(
    private val remoteSource: SalesInvoiceRemoteSource,
    private val localDao: SalesInvoiceDao
) : ISaleInvoiceRepository {

    override suspend fun getPendingInvoices(info: PendingInvoiceInput): Flow<PagingData<PendingInvoiceBO>> {
        return remoteSource.getAllInvoices(info.pos, info.query, info.date).toPagingBO()
    }

    override suspend fun getInvoiceDetail(invoiceId: String): PendingInvoiceBO {
        val invoice = localDao.getInvoiceByName(invoiceId)?.invoice
            ?: throw IllegalArgumentException("Invoice not found locally: $invoiceId")
        return invoice.toBO()
        return PendingInvoiceBO.from(invoice) // mapear a BO según tu modelo
    }

    override suspend fun getAllLocalInvoices(): List<SalesInvoiceWithItemsAndPayments> {
        return localDao.getAllInvoices()
    }

    override suspend fun getInvoiceByName(invoiceName: String): SalesInvoiceWithItemsAndPayments? {
        return localDao.getInvoiceByName(invoiceName)
    }

    override suspend fun saveInvoiceLocally(
        invoice: SalesInvoiceEntity,
        items: List<SalesInvoiceItemEntity>,
        payments: List<POSInvoicePaymentEntity>
    ) {
        localDao.insertFullInvoice(invoice, items, payments)
    }

    override suspend fun markAsSynced(invoiceName: String) {
        localDao.updateSyncStatus(invoiceName, "Synced")
    }

    override suspend fun markAsFailed(invoiceName: String) {
        localDao.updateSyncStatus(invoiceName, "Failed")
    }

    override suspend fun getPendingSyncInvoices(): List<SalesInvoiceEntity> {
        return localDao.getPendingSyncInvoices()
    }

    override suspend fun fetchRemoteInvoices(limit: Int, offset: Int) {
        remoteSource.fetchInvoices(
            limit = limit,
            offset = offset,
            baseUrl = remoteSource.baseUrl,
            headers = remoteSource.headers
        )
    }

    override suspend fun fetchRemoteInvoices(name: String): SalesInvoiceWithItemsAndPayments {
        remoteSource.fetchInvoice(name)
        return localDao.getInvoiceByName(name)
            ?: throw IllegalStateException("Invoice not found after fetch: $name")
    }

    override suspend fun createRemoteInvoice(invoice: SalesInvoiceDto): SalesInvoiceDto {
        val created = remoteSource.createInvoice(
            invoice.toEntities(),
        )
        return created.toDto()
    }

    override suspend fun updateRemoteInvoice(
        invoiceName: String,
        invoice: SalesInvoiceDto
    ): SalesInvoiceDto {
        val updated = remoteSource.updateInvoice(invoice.toEntities())
        return updated.toDto()
    }

    override suspend fun deleteRemoteInvoice(invoiceName: String) {
        remoteSource.deleteInvoice(invoiceName)
        localDao.deleteInvoiceByName(invoiceName)
    }

    override suspend fun syncPendingInvoices() {
        val pending = getPendingSyncInvoices()
        pending.forEach { invoice ->
            try {
                val dto = invoice.toEntities().toDto()
                createRemoteInvoice(dto)
                markAsSynced(invoice.invoiceName ?: "")
            } catch (e: Exception) {
                markAsFailed(invoice.invoiceName ?: "")
            }
        }
    }
}
