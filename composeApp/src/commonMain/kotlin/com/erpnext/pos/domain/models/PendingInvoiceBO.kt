package com.erpnext.pos.domain.models

data class PendingInvoiceBO(
    val invoiceId: String,
    val customerId: String,
    val customer: String?,
    val customerPhone: String?,
    val postingDate: String,
    val dueDate: String?,
    val outstandingAmount: Double,
    val netTotal: Double,
    val total: Double,
    val paidAmount: Double,
    val isPos: Boolean,
    val docStatus: Int,
    val currency: String,
    val status: String? = "Borrador"
)