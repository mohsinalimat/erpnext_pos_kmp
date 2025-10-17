package com.erpnext.pos.remoteSource.datasources

import com.erpnext.pos.localSource.dao.CustomerDao
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.mapper.toEntity

class CustomerRemoteSource(
    private val api: APIService,
    private val customerDao: CustomerDao
) {
    suspend fun fetchAndCacheCustomers() {
        val customers = api.getCustomers()
        val entities = customers.map { dto ->
            val outstanding = api.getCustomerOutstanding(dto.name)
            val creditLimit = dto.creditLimits?.firstOrNull()?.creditLimit ?: 0.0
            val totalPendingAmount = outstanding.pendingInvoices.sumOf { it.outstandingAmount }
            val availableCredit = creditLimit - outstanding.totalOutstanding
            val address = api.getCustomerAddress(dto.name)

            dto.toEntity(
                creditLimit = creditLimit,
                availableCredit = availableCredit,
                pendingInvoicesCount = outstanding.pendingInvoices.size,
                totalPendingAmount = totalPendingAmount,
                address = address
            )
        }
        customerDao.insertAll(entities)
    }
}