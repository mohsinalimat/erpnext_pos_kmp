package com.erpnext.pos.localSource.dao

import androidx.room.*
import com.erpnext.pos.localSource.entities.POSInvoicePaymentEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceItemEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceWithItemsAndPayments

@Dao
interface SalesInvoiceDao {

    // 🔹 Inserciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SalesInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SalesInvoiceItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<POSInvoicePaymentEntity>)

    // 🔹 Inserción transaccional completa
    @Transaction
    suspend fun insertFullInvoice(
        invoice: SalesInvoiceEntity,
        items: List<SalesInvoiceItemEntity>,
        payments: List<POSInvoicePaymentEntity> = emptyList()
    ) {
        insertInvoice(invoice)
        insertItems(items)
        if (payments.isNotEmpty()) insertPayments(payments)
    }

    // 🔹 Consultas
    @Transaction
    @Query("SELECT * FROM tabSalesInvoice ORDER BY posting_date DESC")
    suspend fun getAllInvoices(): List<SalesInvoiceWithItemsAndPayments>

    @Transaction
    @Query("SELECT * FROM tabSalesInvoice WHERE invoice_name = :invoiceName LIMIT 1")
    suspend fun getInvoiceByName(invoiceName: String): SalesInvoiceWithItemsAndPayments?

    @Query("SELECT * FROM tabSalesInvoice WHERE sync_status = 'Pending'")
    suspend fun getPendingSyncInvoices(): List<SalesInvoiceEntity>

    @Query("UPDATE tabSalesInvoice SET sync_status = :status WHERE invoice_name = :invoiceName")
    suspend fun updateSyncStatus(invoiceName: String, status: String)

    // 🔹 Métricas financieras
    @Query("SELECT SUM(grand_total) FROM tabSalesInvoice WHERE posting_date = :date AND docstatus = 1")
    suspend fun getTotalSalesForDate(date: String): Double?

    @Query("SELECT SUM(outstanding_amount) FROM tabSalesInvoice WHERE status IN ('Draft','Submitted')")
    suspend fun getTotalOutstanding(): Double?

    // 🔹 Limpieza / control
    @Query("DELETE FROM tabSalesInvoice WHERE docstatus = 2") // Cancelled
    suspend fun deleteCancelledInvoices()
}
