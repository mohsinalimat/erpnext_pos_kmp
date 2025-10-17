package com.erpnext.pos.data.repositories

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.repositories.ICustomerRepository
import com.erpnext.pos.localSource.datasources.CustomerLocalSource
import com.erpnext.pos.remoteSource.datasources.CustomerRemoteSource
import com.erpnext.pos.remoteSource.mapper.toBO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepository(
    private val remoteSource: CustomerRemoteSource,
    private val localSource: CustomerLocalSource
) : ICustomerRepository {
    override suspend fun getCustomers() {
        remoteSource.fetchAndCacheCustomers()
    }

    override suspend fun getCustomersFiltered(
        search: String,
        territory: String?
    ): Flow<List<CustomerBO>> {
        return if (territory.isNullOrEmpty()) {
            localSource.getAllFiltered(search).map { it.map { entity -> entity.toBO() } }
        } else {
            localSource.getByTerritory(territory, search)
                .map { it.map { entity -> entity.toBO() } }
        }
    }

    override suspend fun getCustomerByName(name: String): CustomerBO? {
        return localSource.getByName(name)?.toBO()
    }
}