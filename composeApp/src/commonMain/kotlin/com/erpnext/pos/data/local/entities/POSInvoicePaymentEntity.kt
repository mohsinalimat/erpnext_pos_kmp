package com.erpnext.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabPOSInvoicePayment")
data class POSInvoicePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceName: String,
    val modeOfPayment: String,
    val amount: Double,
    val changeAmount: Double? = null
)