package com.erpnext.pos.domain.repositories

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import kotlinx.coroutines.flow.Flow

interface IInventoryRepository {
    suspend fun getItems(warehouseId: String): Flow<PagingData<ItemBO>>
    suspend fun getItemDetails(itemId: String): ItemBO
    suspend fun getCategories(): List<CategoryBO>
}