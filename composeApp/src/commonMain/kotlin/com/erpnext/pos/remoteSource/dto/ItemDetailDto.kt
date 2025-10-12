package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetailDto(
    @SerialName("item_code")
    val actualQty: Double,
    @SerialName("rate")
    val rate: Double,
    @SerialName("item_name")
    val itemName: String
)