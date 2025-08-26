package com.erpnext.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabPOSInvoice")
data class POSInvoiceEntity(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val customer: String,
    val customerName: String,
    val postingDate: String,
    val dueDate: String,
    val status: String,
    val total: Double,
    val outstandingAmount: Double? = null
)