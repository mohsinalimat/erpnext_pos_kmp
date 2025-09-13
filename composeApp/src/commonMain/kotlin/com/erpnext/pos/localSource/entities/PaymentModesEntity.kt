package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabPaymentModes")
data class PaymentModesEntity(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val default: Boolean,
    @ColumnInfo("mode_of_payment")
    val modeOfPayment: String,
)