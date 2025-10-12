package com.erpnext.pos.remoteSource.dto

import io.ktor.util.date.GMTDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceDetailsDto(
    @SerialName("mode_of_payment")
    val modeOfPayment: String,
    @SerialName("opening_amount")
    val openingAmount: Double
)

@Serializable
data class POSOpeningEntryDto(
    @SerialName("pos_profile")
    val posProfile: String,
    val company: String,
    @SerialName("period_start_date")
    val periodStartDate: Long,
    val user: String,
    val status: Boolean,
    @SerialName("balance_details")
    val balanceDetails: List<BalanceDetailsDto>,
)