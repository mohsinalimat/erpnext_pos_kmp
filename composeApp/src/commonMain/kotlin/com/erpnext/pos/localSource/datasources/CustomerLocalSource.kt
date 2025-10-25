package com.erpnext.pos.localSource.datasources

import com.erpnext.pos.localSource.entities.CustomerEntity
import com.erpnext.pos.localSource.dao.CustomerDao
import kotlinx.coroutines.flow.Flow

interface ICustomerLocalSource {
    suspend fun insertAll(customers: List<CustomerEntity>)
    suspend fun getAll(): Flow<List<CustomerEntity>>
    fun getAllFiltered(search: String): Flow<List<CustomerEntity>>
    fun getByTerritory(territory: String, search: String?): Flow<List<CustomerEntity>>
    suspend fun getByName(name: String): CustomerEntity?
    suspend fun count(): Int
}

class CustomerLocalSource(private val dao: CustomerDao) : ICustomerLocalSource {
    override suspend fun insertAll(customers: List<CustomerEntity>) = dao.insertAll(customers)

    override suspend fun getAll(): Flow<List<CustomerEntity>> = dao.getAll()

    override fun getAllFiltered(search: String): Flow<List<CustomerEntity>> =
        dao.getAllFiltered(search)

    override fun getByTerritory(territory: String, search: String?): Flow<List<CustomerEntity>> =
        dao.getByTerritory(territory, search)

    override suspend fun getByName(name: String): CustomerEntity? = dao.getByName(name)

    override suspend fun count(): Int {
        return dao.count()
    }
}