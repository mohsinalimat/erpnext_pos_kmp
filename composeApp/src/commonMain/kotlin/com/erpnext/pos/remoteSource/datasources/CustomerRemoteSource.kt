package com.erpnext.pos.remoteSource.datasources

import com.erpnext.pos.localSource.dao.CustomerDao
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.ContactChildDto
import com.erpnext.pos.remoteSource.mapper.toEntity

class CustomerRemoteSource(
    private val api: APIService,
    private val customerDao: CustomerDao
) {
    suspend fun fetchAndCacheCustomers(territory: String) {
        val customers = api.getCustomers(territory)
        val entities = customers.map { dto ->
            val outstanding = api.getCustomerOutstanding(dto.name)
            val creditLimit = dto.creditLimit
            val totalPendingAmount = outstanding.pendingInvoices.sumOf { it.total - it.paidAmount }
            val availableCredit = (creditLimit ?: 0.0) - outstanding.totalOutstanding
            //val address = api.getCustomerAddress(dto.name)
            //val contact = api.getCustomerContact(dto.name)

            dto.toEntity(
                creditLimit = creditLimit,
                availableCredit = availableCredit,
                pendingInvoicesCount = outstanding.pendingInvoices.size,
                totalPendingAmount = totalPendingAmount,
                address = "DDF Cine Cabrera, 150 metros arriba, calle 27 de Mayo", // address,
                contact = ContactChildDto(
                    "Contacto",
                    "89517503",
                    "test@gmail.com"
                ) //contact.firstOrNull()
            )
        }
        customerDao.insertAll(entities)
    }
}