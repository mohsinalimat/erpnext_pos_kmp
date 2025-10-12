package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemPriceDto(
    @SerialName("item_code")
    val itemCode: String? = null,
    @SerialName("price_list_rate")
    val priceListRate: Double
)