package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingInvoiceDto(
    @SerialName("name")
    val name: String,
    @SerialName("outstanding_amount")
    val outstandingAmount: Double,
    @SerialName("due_date")
    val dueDate: String,
    @SerialName("status")
    val status: String
)