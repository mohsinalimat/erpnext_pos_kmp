package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabPosProfile")
data class POSProfileEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "profile_name")
    val profileName: String,

    @ColumnInfo(name = "warehouse")
    val warehouse: String,

    @ColumnInfo(name = "country")
    val country: String,

    @ColumnInfo(name = "disabled")
    val disabled: Boolean,

    @ColumnInfo(name = "company")
    val company: String,

    @ColumnInfo(name = "default_currency")
    val defaultCurrency: String,

    // Sales config
    @ColumnInfo(name = "customer_group")
    val customerGroup: String? = null,

    @ColumnInfo(name = "price_list")
    val priceList: String? = null,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "default_customer")
    val defaultCustomer: String? = null,

    @ColumnInfo(name = "active")
    val active: Boolean? = false,

    @ColumnInfo(name = "user")
    val user: String,

    @ColumnInfo(name = "route")
    val route: String
)