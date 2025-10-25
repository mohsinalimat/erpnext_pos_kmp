package com.erpnext.pos.data.repositories

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.repositories.ICustomerRepository
import com.erpnext.pos.localSource.datasources.CustomerLocalSource
import com.erpnext.pos.remoteSource.datasources.CustomerRemoteSource
import com.erpnext.pos.remoteSource.mapper.toBO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CustomerRepository(
    private val remoteSource: CustomerRemoteSource,
    private val localSource: CustomerLocalSource
) : ICustomerRepository {

    override suspend fun getCustomers(
        territory: String?,
        search: String?
    ): Flow<List<CustomerBO>> = flow {

        // 1️⃣ Verificamos si hay datos locales
        val localCount = localSource.count()
        val needSync = localCount == 0

        if (needSync) {
            try {
                // 2️⃣ Si no hay datos, obtenemos desde el servidor
                remoteSource.fetchAndCacheCustomers(territory ?: "")
            } catch (e: Exception) {
                // Maneja error de red (pero continuamos con lo que haya local)
            }
        }

        // 3️⃣ Ahora, obtenemos desde la DB con los filtros dinámicos
        val flow = when {
            territory.isNullOrEmpty() && search.isNullOrEmpty() ->
                localSource.getAll() // sin filtros
            territory.isNullOrEmpty() ->
                localSource.getAllFiltered(search!!)

            search.isNullOrEmpty() ->
                localSource.getByTerritory(territory!!, null)

            else ->
                localSource.getByTerritory(territory!!, search!!)
        }

        // 4️⃣ Emitimos los datos convertidos al modelo de negocio
        emitAll(
            flow.map { entities -> entities.map { it.toBO() } }
        )
    }

    override suspend fun getCustomerByName(name: String): CustomerBO? {
        return localSource.getByName(name)?.toBO()
    }
}