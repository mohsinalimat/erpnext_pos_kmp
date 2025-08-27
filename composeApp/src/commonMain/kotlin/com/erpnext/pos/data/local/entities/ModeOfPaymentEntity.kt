package com.erpnext.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabModeOfPayment")
data class ModeOfPaymentEntity(

    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "mode_of_payment")
    val modeOfPayment: String, // e.g. "Cash", "Card", "Bank Transfer"

    @ColumnInfo(name = "type")
    val type: String = "Cash", // "Cash" | "Bank" | "Card" | "Wallet" etc.

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,

    @ColumnInfo(name = "currency")
    val currency: String? = null, // can be null if multi-currency not enforced

    @ColumnInfo(name = "account")
    val account: String? = null,  // GL Account in ERPNext
)