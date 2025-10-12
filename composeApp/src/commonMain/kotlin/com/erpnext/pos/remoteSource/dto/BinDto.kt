package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinDto(
    @SerialName("item_code")
    val itemCode: String,
    @SerialName("actual_qty")
    val actualQty: Double,
)