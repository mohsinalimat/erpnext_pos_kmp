package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SalesInvoiceDto(
    val name: String? = null,
    val customer: String,
    val company: String,
    val posting_date: String,
    val due_date: String? = null,
    val currency: String,
    val status: String? = null,
    val grand_total: Double = 0.0,
    val outstanding_amount: Double = 0.0,
    val total_taxes_and_charges: Double = 0.0,
    val items: List<SalesInvoiceItemDto> = emptyList(),
    @SerialName("payments")
    val payments: List<SalesInvoicePaymentDto> = emptyList(),
    val remarks: String? = null,
    val is_pos: Boolean = true,
    val doctype: String = "Sales Invoice"
)

@Serializable
data class SalesInvoiceItemDto(
    val item_code: String,
    val item_name: String? = null,
    val description: String? = null,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val discount_percentage: Double? = null,
    val warehouse: String? = null,
    val income_account: String? = null,
    val cost_center: String? = null
)

@Serializable
data class InvoiceTax(
    @SerialName("charge_type") val chargeType: String = "On Net Total",
    @SerialName("account_head") val accountHead: String,
    val rate: Double,
    @SerialName("description") val description: String? = null
)

@Serializable
data class SalesInvoicePaymentDto(
    val mode_of_payment: String,
    val amount: Double,
    val type: String? = "Receive"
)