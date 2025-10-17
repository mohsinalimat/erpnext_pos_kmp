package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    @SerialName("name")
    val name: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("territory")
    val territory: String,
    @SerialName("mobile_no")
    val mobileNo: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("customer_type")
    val customerType: String,
    @SerialName("disabled")
    val disabled: Boolean = false,
    @SerialName("credit_limits")
    val creditLimits: List<CreditLimitChildDto>? = null,
    @SerialName("addresses")  // Fetch separate, but include if method returns
    val addresses: List<AddressChildDto>? = null
)