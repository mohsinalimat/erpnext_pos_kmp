@file:OptIn(ExperimentalTime::class)

package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.localSource.entities.POSInvoicePaymentEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceItemEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceWithItemsAndPayments
import com.erpnext.pos.remoteSource.dto.SalesInvoiceDto
import com.erpnext.pos.remoteSource.dto.SalesInvoiceItemDto
import com.erpnext.pos.remoteSource.dto.SalesInvoicePaymentDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ------------------------------------------------------
// 🔹 Local → Remoto (para enviar al API ERPNext)
// ------------------------------------------------------

fun SalesInvoiceWithItemsAndPayments.toDto(): SalesInvoiceDto {
    return SalesInvoiceDto(
        name = invoice.invoiceName,
        customer = invoice.customer,
        company = invoice.company,
        posting_date = invoice.postingDate,
        due_date = invoice.dueDate,
        currency = invoice.currency,
        status = invoice.status,
        grand_total = invoice.grandTotal,
        outstanding_amount = invoice.outstandingAmount,
        total_taxes_and_charges = invoice.taxTotal,
        items = items.map { it.toDto(invoice) },
        payments = payments.map { it.toDto() },
        remarks = invoice.remarks,
        is_pos = true,
        doctype = "Sales Invoice"
    )
}

fun SalesInvoiceItemEntity.toDto(parent: SalesInvoiceEntity): SalesInvoiceItemDto {
    return SalesInvoiceItemDto(
        item_code = itemCode,
        item_name = itemName,
        description = description,
        qty = qty,
        rate = rate,
        amount = amount,
        discount_percentage = discountPercentage.takeIf { it != 0.0 },
        warehouse = warehouse ?: parent.warehouse,
        income_account = parent.incomeAccount,
        cost_center = parent.costCenter
    )
}

fun POSInvoicePaymentEntity.toDto(): SalesInvoicePaymentDto {
    return SalesInvoicePaymentDto(
        mode_of_payment = modeOfPayment,
        amount = amount,
        type = "Receive"
    )
}

// ------------------------------------------------------
// 🔹 Remoto → Local (para guardar respuesta del servidor)
// ------------------------------------------------------

fun SalesInvoiceDto.toEntities(): SalesInvoiceWithItemsAndPayments {
    val now = Clock.System.now().toEpochMilliseconds()

    val invoiceEntity = SalesInvoiceEntity(
        invoiceName = name,
        customer = customer,
        company = company,
        postingDate = posting_date,
        dueDate = due_date,
        currency = currency,
        netTotal = items.sumOf { it.amount },
        taxTotal = total_taxes_and_charges,
        grandTotal = grand_total,
        outstandingAmount = outstanding_amount,
        status = status ?: "Draft",
        syncStatus = "Synced",
        docstatus = if (status == "Submitted" || status == "Paid") 1 else 0,
        modeOfPayment = payments.firstOrNull()?.mode_of_payment,
        createdAt = now,
        modifiedAt = now,
        remarks = remarks
    )

    val itemsEntity = items.map { dto ->
        SalesInvoiceItemEntity(
            parentInvoice = name ?: "",
            itemCode = dto.item_code,
            itemName = dto.item_name,
            description = dto.description,
            qty = dto.qty,
            rate = dto.rate,
            amount = dto.amount,
            discountPercentage = dto.discount_percentage ?: 0.0,
            warehouse = dto.warehouse,
            incomeAccount = dto.income_account,
            costCenter = dto.cost_center,
            createdAt = now,
            modifiedAt = now
        )
    }

    val paymentsEntity = payments.map { dto ->
        POSInvoicePaymentEntity(
            parentInvoice = name ?: "",
            modeOfPayment = dto.mode_of_payment,
            amount = dto.amount,
            createdAt = now
        )
    }

    return SalesInvoiceWithItemsAndPayments(
        invoice = invoiceEntity,
        items = itemsEntity,
        payments = paymentsEntity
    )
}
