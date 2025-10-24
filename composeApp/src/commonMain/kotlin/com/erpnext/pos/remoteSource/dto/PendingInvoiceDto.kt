package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingInvoiceDto(
    @SerialName("name")
    val name: String,
    @SerialName("customer")
    val customerId: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("contact_mobile")
    val customerPhone: String?,
    @SerialName("posting_date")
    val postingDate: String,
    @SerialName("due_date")
    val dueDate: String,
    @SerialName("net_total")
    val netTotal: Double,
    @SerialName("grand_total")
    val total: Double,
    @SerialName("paid_amount")
    val paidAmount: Double,
    @SerialName("outstanding_amount")
    val outstandingAmount: Double,
    @SerialName("pos_profile")
    val posProfile: String?,
    @SerialName("party_account_currency")
    val currency: String?,
    @SerialName("is_pos")
    @Serializable(IntAsBooleanSerializer::class)
    val isPos: Boolean,
    @SerialName("docstatus")
    val docStatus: Int,
    @SerialName("status")
    val status: String
)