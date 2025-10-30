package com.erpnext.pos.localSource.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.PendingSalesInvoiceEntity

@Dao
interface PendingInvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invoices: List<PendingSalesInvoiceEntity>)

    @Query("SELECT * FROM tabPendingSalesInvoice ORDER BY posting_date DESC")
    fun getAll(): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT * FROM tabPendingSalesInvoice WHERE invoice_id = :invoiceName")
    fun getInvoiceDetails(invoiceName: String): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT * FROM tabPendingSalesInvoice WHERE customer_name LIKE '%' || :search || '%' ORDER BY customer_name ASC")
    fun getAllFiltered(search: String): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT * FROM tabPendingSalesInvoice WHERE posting_date BETWEEN :startDate AND :endDate ORDER BY posting_date DESC")
    fun getInvoicesByDateRange(
        startDate: String,
        endDate: String
    ): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT * FROM tabPendingSalesInvoice WHERE due_date < :today AND outstanding_amount > 0 ORDER BY due_date ASC")
    fun getOverdueInvoices(today: String): PagingSource<Int, PendingSalesInvoiceEntity>


    @Query("SELECT * FROM tabPendingSalesInvoice WHERE posting_date BETWEEN :startDate AND :endDate AND due_date < :today AND outstanding_amount > 0 ORDER BY due_date ASC")
    fun getOverdueInvoicesInRange(
        startDate: String, endDate: String, today: String
    ): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT * FROM tabPendingSalesInvoice WHERE (:query IS NULL OR customer_name LIKE '%' || :query || '%' OR invoice_id LIKE '%' || :query || '%') AND ((:date IS NULL OR posting_date == :date)) ORDER BY posting_date DESC")
    fun getFilteredInvoices(
        query: String?,
        date: String?,
    ): PagingSource<Int, PendingSalesInvoiceEntity>

    @Query("SELECT COUNT(*) FROM tabPendingSalesInvoice")
    suspend fun countAll(): Int

    @Query("DELETE FROM tabPendingSalesInvoice")
    suspend fun deleteAll()
}