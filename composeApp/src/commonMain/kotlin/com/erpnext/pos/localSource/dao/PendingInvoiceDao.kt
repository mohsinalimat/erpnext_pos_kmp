package com.erpnext.pos.localSource.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.SalesInvoiceEntity

@Dao
interface PendingInvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invoices: List<SalesInvoiceEntity>)

    @Query("SELECT * FROM tabSalesInvoice ORDER BY posting_date DESC")
    fun getAll(): PagingSource<Int, SalesInvoiceEntity>

    @Query("SELECT * FROM tabSalesInvoice WHERE customer_name LIKE '%' || :search || '%' ORDER BY customer_name ASC")
    fun getAllFiltered(search: String): PagingSource<Int, SalesInvoiceEntity>

    @Query("SELECT * FROM tabSalesInvoice WHERE posting_date BETWEEN :startDate AND :endDate ORDER BY posting_date DESC")
    fun getInvoicesByDateRange(
        startDate: String,
        endDate: String
    ): PagingSource<Int, SalesInvoiceEntity>

    @Query("SELECT * FROM tabSalesInvoice WHERE due_date < :today AND outstanding_amount > 0 ORDER BY due_date ASC")
    fun getOverdueInvoices(today: String): PagingSource<Int, SalesInvoiceEntity>


    @Query("SELECT * FROM tabSalesInvoice WHERE posting_date BETWEEN :startDate AND :endDate AND due_date < :today AND outstanding_amount > 0 ORDER BY due_date ASC")
    fun getOverdueInvoicesInRange(
        startDate: String, endDate: String, today: String
    ): PagingSource<Int, SalesInvoiceEntity>

    @Query("SELECT * FROM tabSalesInvoice WHERE (:query IS NULL OR customer_name LIKE '%' || :query || '%' OR invoice_id LIKE '%' || :query || '%') AND ((:date IS NULL OR posting_date >= :date)) ORDER BY posting_date DESC")
    fun getFilteredInvoices(
        query: String?,
        date: String?,
    ): PagingSource<Int, SalesInvoiceEntity>

    @Query("SELECT COUNT(*) FROM tabSalesInvoice")
    suspend fun countAll(): Int

    @Query("DELETE FROM tabSalesInvoice")
    suspend fun deleteAll()
}