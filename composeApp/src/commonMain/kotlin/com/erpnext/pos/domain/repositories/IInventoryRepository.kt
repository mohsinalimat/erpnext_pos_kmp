package com.erpnext.pos.domain.repositories

import androidx.paging.PagingData
import com.erpnext.pos.base.Resource
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import kotlinx.coroutines.flow.Flow

interface IInventoryRepository {
    suspend fun getItems(
        warehouseId: String?,
        priceList: String?,
        query: String?,
    ): Flow<PagingData<ItemBO>>

    suspend fun getItemDetails(itemId: String): ItemBO
    suspend fun getCategories(): Flow<Resource<List<CategoryBO>>>
}