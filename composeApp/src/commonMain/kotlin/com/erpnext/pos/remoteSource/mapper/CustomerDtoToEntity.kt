package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.domain.models.CreditLimit
import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.localSource.entities.CustomerEntity
import com.erpnext.pos.remoteSource.dto.CreditLimitChildDto
import com.erpnext.pos.remoteSource.dto.CustomerDto

fun CustomerDto.toEntity(
    creditLimit: Double,
    availableCredit: Double,
    pendingInvoicesCount: Int,
    totalPendingAmount: Double,
    address: String
): CustomerEntity = CustomerEntity(
    name = name,
    customerName = customerName,
    territory = territory,
    mobileNo = mobileNo,
    phone = phone,
    customerType = customerType,
    creditLimit = creditLimit,
    currentBalance = totalPendingAmount,  // Map totalPendingAmount a currentBalance
    totalPendingAmount = totalPendingAmount,
    pendingInvoicesCount = pendingInvoicesCount,
    availableCredit = availableCredit,
    address = address
)

fun CustomerEntity.toBO(): CustomerBO = CustomerBO(
    name = name,
    customerName = customerName,
    territory = territory,
    mobileNo = mobileNo,
    phone = phone,
    customerType = customerType,
    creditLimit = listOf(CreditLimit(creditLimit = creditLimit)),
    address = address,
    currentBalance = currentBalance,
    pendingInvoices = totalPendingAmount,  // Total monto pendiente
    availableCredit = availableCredit
)