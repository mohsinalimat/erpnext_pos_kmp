package com.erpnext.pos.domain.repositories

import com.erpnext.pos.domain.models.POSProfileBO

interface IPOSRepository {
    suspend fun getPOSProfileInfo(): List<POSProfileBO>
}