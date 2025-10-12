package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO: Aca tengo que agregar los campos verdaderos que tengo en
//TODO: ItemDto/ItemEntity para que todo quede tal cual pero con la
//TODO: Combination de Precios y Stock
@Serializable
data class WarehouseItemDto(
    @SerialName("item_code")
    val itemCode: String,
    @SerialName("actual_qty")
    val actualQty: Double,
    @SerialName("price")
    val price: Double,
)