package com.erpnext.pos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabItem")
data class ItemEntity (
    @PrimaryKey
    var id: String,
    var name: String = "",
    var description: String,
    var barcode: String = "",
    var image: String = "",
    var price: Double = 0.0,
    var discount: Double = 0.0,
    var isService: Boolean = false,
    var isStocked: Boolean = false,
    var uom: String,
)