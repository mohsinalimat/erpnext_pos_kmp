package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class POSProfileSimpleDto(
    @SerialName("name") val profileName: String,
    @SerialName("warehouse")
    val warehouse: String,
    val country: String,
    @Serializable(with = IntAsBooleanSerializer::class) val disabled: Boolean,
    val company: String,
    val currency: String,
)

@Serializable
data class POSProfileDto(
    @SerialName("name") val profileName: String,
    @SerialName("warehouse")
    val warehouse: String,
    @SerialName("route")
    val route: String,
    val country: String,
    @Serializable(with = IntAsBooleanSerializer::class) val disabled: Boolean,
    val company: String,
    val currency: String,
    @SerialName("payments") val payments: List<PaymentModesDto>
)

@Serializable
data class PaymentModesDto(
    val name: String,
    @Serializable(with = IntAsBooleanSerializer::class) val default: Boolean,
    @SerialName("mode_of_payment") val modeOfPayment: String,
)