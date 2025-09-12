package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class POSProfileEntryDto(
    @SerialName("pos_profile")
    val posProfile: String,
    val user: String,
    val company: String,
    val warehouse: String,
    @SerialName("selling_price_list")
    val sellingPriceList: String,
    val currency: String,
    val status: String,
    @SerialName("opening_amount")
    val openingAmount: Double
)