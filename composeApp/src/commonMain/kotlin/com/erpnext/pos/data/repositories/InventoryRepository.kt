package com.erpnext.pos.data.repositories

import androidx.paging.PagingData
import com.erpnext.pos.data.mappers.toBO
import com.erpnext.pos.data.mappers.toPagingBO
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import com.erpnext.pos.remoteSource.datasources.InventoryRemoteSource
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val remoteSource: InventoryRemoteSource
) : IInventoryRepository {

    override suspend fun getItems(warehouseId: String?, priceList: String?): Flow<PagingData<ItemBO>> {
        return remoteSource.getItems(warehouseId, priceList).toPagingBO()
    }

    override suspend fun getItemDetails(itemId: String): ItemBO {
        return remoteSource.getItemDetail(itemId).toBO()
    }

    override suspend fun getCategories(): List<CategoryBO> {
        return remoteSource.getCategories().toBO()
    }
}