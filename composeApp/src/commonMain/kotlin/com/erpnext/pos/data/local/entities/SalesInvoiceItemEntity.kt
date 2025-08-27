package com.erpnext.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "tabSalesInvoice")
data class SalesInvoiceEntity @OptIn(ExperimentalTime::class) constructor(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,   // Local UUID

    @ColumnInfo(name = "posting_date")
    val postingDate: String, // "YYYY-MM-DD"

    @ColumnInfo(name = "posting_time")
    val postingTime: String, // "HH:mm:ss"

    @ColumnInfo(name = "customer")
    val customer: String,

    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,

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
    @ColumnInfo(name = "docstatus")
    val docStatus: Int = 0, // 0 = Draft, 1 = Submitted, 2 = Cancelled

    @ColumnInfo(name = "is_pos")
    val isPOS: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()

)