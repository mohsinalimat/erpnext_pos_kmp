package com.erpnext.pos.data.repositories

import androidx.paging.PagingData
import androidx.paging.map
import com.erpnext.pos.base.Resource
import com.erpnext.pos.base.networkBoundResource
import com.erpnext.pos.data.mappers.toBO
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import com.erpnext.pos.localSource.datasources.InventoryLocalSource
import com.erpnext.pos.remoteSource.datasources.InventoryRemoteSource
import com.erpnext.pos.remoteSource.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepository(
    private val remoteSource: InventoryRemoteSource,
    private val localSource: InventoryLocalSource,
) : IInventoryRepository {

    override suspend fun getItems(
        warehouseId: String?,
        priceList: String?,
        query: String?,
    ): Flow<PagingData<ItemBO>> {
        return remoteSource.getItems(warehouseId, priceList, query)
            .map { it.map { item -> item.toBO() } }
    }

    override suspend fun getItemDetails(itemId: String): ItemBO {
        return remoteSource.getItemDetail(itemId).toBO()
    }

    override suspend fun getCategories(): Flow<Resource<List<CategoryBO>>> = networkBoundResource(
        query = {
            localSource.getItemCategories().map { entities -> entities.map { it.toBO() } }
        },
        fetch = { remoteSource.getCategories() },
        saveFetchResult = { categories ->
            localSource.deleteAllCategories()
            localSource.insertCategories(categories.map { it.toEntity() })
        },
        shouldFetch = { cached -> cached.isEmpty() },
        onFetchFailed = { e ->
            print("Fallo la sincronizacion de categorias -> ${e.message}")
        }
    )
}