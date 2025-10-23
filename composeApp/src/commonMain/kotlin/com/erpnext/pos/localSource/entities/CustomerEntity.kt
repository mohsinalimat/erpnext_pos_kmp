package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val customerName: String,
    val territory: String?,
    val email: String?,
    val mobileNo: String?,
    val customerType: String,
    val creditLimit: Double? = null,
    val currentBalance: Double,
    val totalPendingAmount: Double,  // Sum outstanding_amount
    val pendingInvoicesCount: Int,
    val availableCredit: Double,
    val address: String? = null  // Formatted
)