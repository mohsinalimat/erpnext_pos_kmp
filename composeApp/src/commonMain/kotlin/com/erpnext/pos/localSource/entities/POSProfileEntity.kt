package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "tabPosProfile")
data class POSProfileEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "profile_name") val profileName: String,

    @ColumnInfo(name = "warehouse") val warehouse: String,

    @ColumnInfo(name = "route") val route: String,

    @ColumnInfo(name = "country") val country: String,

    @ColumnInfo(name = "company") val company: String,

    @ColumnInfo(name = "currency") val currency: String,

    @ColumnInfo("income_account") val incomeAccount: String,

    @ColumnInfo("expense_account") val expenseAccount: String,

    @ColumnInfo("branch") val branch: String,

    @ColumnInfo("apply_discount_on") val applyDiscountOn: String,

    @ColumnInfo("cost_center") val costCenter: String,

    @ColumnInfo("selling_price_list") val sellingPriceList: String,

    @ColumnInfo(name = "active") val active: Boolean? = false,

    @ColumnInfo(name = "user") val user: String,
)

data class POSProfileWithPaymentsInfo(
    @Embedded val profile: POSProfileEntity, @Relation(
        parentColumn = "profile_name", entityColumn = "profile_id"
    ) val payments: List<POSInvoicePaymentEntity>
)