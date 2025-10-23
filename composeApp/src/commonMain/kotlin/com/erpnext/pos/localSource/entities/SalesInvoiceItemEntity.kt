package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "tabSalesInvoice")
data class SalesInvoiceEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "invoice_id")
    val invoiceId: String,

    @ColumnInfo(name = "posting_date")
    val postingDate: String, // "YYYY-MM-DD"

    @ColumnInfo(name = "posting_time")
    val postingTime: String, // "HH:mm:ss"

    @ColumnInfo(name = "customer")
    val customer: String,

    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,

    @ColumnInfo(name = "customer_phone")
    val customerPhone: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: String? = null,

    @ColumnInfo(name = "currency")
    val currency: String = "USD",

    @ColumnInfo(name = "net_total")
    val netTotal: Double = 0.0,

    @ColumnInfo(name = "grand_total")
    val grandTotal: Double = 0.0,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: Double = 0.0,

    @ColumnInfo(name = "outstanding_amount")
    val outstandingAmount: Double = 0.0,

    // Sync and status
    @ColumnInfo(name = "doc_status")
    val docStatus: Int = 0, // 0 = Draft, 1 = Submitted, 2 = Cancelled

    @ColumnInfo(name = "status")
    val status: String? = null,

    @ColumnInfo(name = "is_pos")
    val isPOS: Boolean = true
)