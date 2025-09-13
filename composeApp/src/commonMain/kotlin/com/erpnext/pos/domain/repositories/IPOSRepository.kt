package com.erpnext.pos.domain.repositories

import com.erpnext.pos.domain.models.POSProfileBO

interface IPOSRepository {
    suspend fun getPOSProfiles(): List<POSProfileBO>
    suspend fun getPOSProfileDetails(profileId: String): POSProfileBO
}