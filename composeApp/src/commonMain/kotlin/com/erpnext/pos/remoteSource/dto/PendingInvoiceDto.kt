package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingInvoiceDto(
    @SerialName("name")
    val name: String,
    @SerialName("customer")
    val customer: String,
    @SerialName("status")
    val status: String,
    @SerialName("due_date")
    val dueDate: String,
    @SerialName("pos_profile")
    val posProfile: String,
    @SerialName("rounded_total")
    val total: Double,
    @SerialName("paid_amount")
    val paidAmount: Double,
    @SerialName("party_account_currency")
    val currency: String
)