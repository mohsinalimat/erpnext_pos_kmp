package com.erpnext.pos.domain.repositories

import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.models.POSProfileSimpleBO

interface IPOSRepository {
    suspend fun getPOSProfiles(): List<POSProfileSimpleBO>
    suspend fun getPOSProfileDetails(profileId: String): POSProfileBO
}