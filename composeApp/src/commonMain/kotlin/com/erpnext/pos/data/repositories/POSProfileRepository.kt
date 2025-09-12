package com.erpnext.pos.data.repositories

import com.erpnext.pos.data.mappers.toBO
import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.repositories.IPOSRepository
import com.erpnext.pos.remoteSource.datasources.POSProfileRemoteSource

//TODO: Siempre tenemos que agregar el LocalSource
class POSProfileRepository(
    private val remoteSource: POSProfileRemoteSource
) : IPOSRepository {
    override suspend fun getPOSProfileInfo(): List<POSProfileBO> {
        return remoteSource.getPOSProfileInfo().toBO()
    }
}