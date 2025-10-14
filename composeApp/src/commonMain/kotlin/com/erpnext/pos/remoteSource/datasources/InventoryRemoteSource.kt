package com.erpnext.pos.remoteSource.datasources

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.paging.InventoryRemoteMediator
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class InventoryRemoteSource(
    private val apiService: APIService,
    private val itemDao: ItemDao,
) {
    fun getItems(
        warehouseId: String? = null,
        priceList: String? = null
    ): Flow<PagingData<ItemEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = InventoryRemoteMediator(apiService, warehouseId, priceList, itemDao)
        ) {
            itemDao.getAllItems()
        }.flow
    }

    suspend fun getItemDetail(itemId: String): ItemDto {
        return apiService.getItemDetail(itemId)
    }

    suspend fun getCategories(): List<CategoryDto> {
        return apiService.getCategories()
    }
}