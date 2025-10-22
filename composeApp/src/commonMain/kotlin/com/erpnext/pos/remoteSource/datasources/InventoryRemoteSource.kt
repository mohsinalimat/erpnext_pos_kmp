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
import kotlinx.coroutines.flow.onStart

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
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            remoteMediator = InventoryRemoteMediator(apiService, itemDao, warehouseId, priceList),
            initialKey = null
        ) {
            itemDao.getAllItems()
        }.flow.onStart {
            emit(PagingData.empty())
        }
    }

    suspend fun getItemDetail(itemId: String): ItemDto {
        return apiService.getItemDetail(itemId)
    }

    suspend fun getCategories(): List<CategoryDto> {
        return apiService.getCategories()
    }
}