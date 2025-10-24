package com.erpnext.pos.domain.repositories

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.CustomerBO
import kotlinx.coroutines.flow.Flow

interface ICustomerRepository {
    // fun getCustomers(): Flow<PagingData<CustomerBO>>
    suspend fun getCustomers(territory: String)
    suspend fun getCustomersFiltered(search: String, territory: String?): Flow<List<CustomerBO>>
    suspend fun getCustomerByName(name: String): CustomerBO?
}