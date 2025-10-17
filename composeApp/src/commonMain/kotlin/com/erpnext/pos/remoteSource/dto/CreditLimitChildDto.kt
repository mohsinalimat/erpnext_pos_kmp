package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreditLimitChildDto(
    @SerialName("credit_limit")
    val creditLimit: Double,
    @SerialName("company")
    val company: String? = null
)

@Serializable
data class AddressChildDto(
    @SerialName("address_line1")
    val addressLine1: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("phone")
    val phone: String? = null  // Additional phone if needed
)