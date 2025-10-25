package com.erpnext.pos.domain.repositories

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.CustomerBO
import kotlinx.coroutines.flow.Flow

interface ICustomerRepository {
    suspend fun getCustomers(
        territory: String?,
        search: String?
    ): Flow<List<CustomerBO>>

    suspend fun getCustomerByName(name: String): CustomerBO?
}