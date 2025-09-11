package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabItem")
data class ItemEntity(
    @PrimaryKey(autoGenerate = false)
    var itemCode: String = "",
    var name: String = "",
    var description: String,
    var barcode: String = "",
    var image: String? = null,
    var itemGroup: String = "",
    var brand: String? = null,
    var price: Double = 0.0,
    var discount: Double = 0.0,
    var isService: Boolean = false,
    var isStocked: Boolean = false,
    var stockUom: String,
)