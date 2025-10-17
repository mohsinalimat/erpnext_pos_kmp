package com.erpnext.pos.localSource.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val name: String,
    val customerName: String,
    val territory: String,
    val mobileNo: String?,
    val phone: String?,
    val customerType: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val totalPendingAmount: Double,  // Sum outstanding_amount
    val pendingInvoicesCount: Int,
    val availableCredit: Double,
    val address: String  // Formatted
)