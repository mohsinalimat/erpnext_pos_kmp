package com.erpnext.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabPOSInvoiceItem")
data class POSInvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceName: String,
    val itemCode: String,
    val itemName: String,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null
)