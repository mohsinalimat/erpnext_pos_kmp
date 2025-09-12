package com.erpnext.pos.remoteSource.datasources

import com.erpnext.pos.localSource.dao.POSProfileDao
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.POSProfileEntryDto
import com.erpnext.pos.remoteSource.mapper.toEntity

class POSProfileRemoteSource(
    private val api: APIService,
    private val posProfileDao: POSProfileDao
) {
    suspend fun getPOSProfileInfo(): List<POSProfileDto> {
        val profiles = api.getPOSProfileInfo()
        if (profiles.isNotEmpty()) {
            posProfileDao.insertAll(profiles.toEntity())
        }
        return profiles
    }

    suspend fun openCashBox(data: POSProfileEntryDto) {
        api.openCashBox(data)
    }
}