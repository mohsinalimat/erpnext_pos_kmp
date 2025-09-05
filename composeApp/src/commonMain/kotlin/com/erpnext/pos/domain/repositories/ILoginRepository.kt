package com.erpnext.pos.domain.repositories

import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.CredentialsDto

interface IInventoryRepository {
    suspend fun getItems() : List<ItemBO>
}