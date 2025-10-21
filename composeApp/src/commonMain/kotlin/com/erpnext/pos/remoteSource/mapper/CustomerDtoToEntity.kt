package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.localSource.entities.CustomerEntity
import com.erpnext.pos.remoteSource.dto.ContactChildDto
import com.erpnext.pos.remoteSource.dto.CustomerDto

fun CustomerDto.toEntity(
    creditLimit: Double? = null,
    availableCredit: Double,
    pendingInvoicesCount: Int,
    totalPendingAmount: Double,
    address: String,
    contact: ContactChildDto?
): CustomerEntity = CustomerEntity(
    name = name,
    customerName = customerName,
    territory = territory,
    mobileNo = contact?.mobileNo ?: "",
    customerType = customerType,
    creditLimit = creditLimit,
    currentBalance = totalPendingAmount,  // Map totalPendingAmount a currentBalance
    totalPendingAmount = totalPendingAmount,
    pendingInvoicesCount = pendingInvoicesCount,
    availableCredit = availableCredit,
    address = address,
    email = contact?.email ?: ""
)

fun CustomerEntity.toBO(): CustomerBO = CustomerBO(
    name = name,
    customerName = customerName,
    territory = territory,
    mobileNo = mobileNo,
    customerType = customerType,
    creditLimit = creditLimit,
    address = address,
    currentBalance = currentBalance,
    pendingInvoices = pendingInvoicesCount,  // Total monto pendiente
    totalPendingAmount = totalPendingAmount,
    availableCredit = availableCredit
)