package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "tabSalesInvoicePayment",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["invoice_name"],
            childColumns = ["parent_invoice"],
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index(value = ["parent_invoice"])
    ]
)
data class POSInvoicePaymentEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "parent_invoice")
    val parentInvoice: String, // invoice_name del padre

    @ColumnInfo(name = "mode_of_payment")
    val modeOfPayment: String, // Cash, Card, Transfer...

    @ColumnInfo(name = "amount")
    val amount: Double = 0.0,

    @ColumnInfo(name = "payment_reference")
    val paymentReference: String? = null, // No. de recibo o transacción
    @ColumnInfo(name = "payment_date")
    val paymentDate: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)