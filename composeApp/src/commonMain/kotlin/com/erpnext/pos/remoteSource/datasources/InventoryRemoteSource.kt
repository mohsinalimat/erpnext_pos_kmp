package com.erpnext.pos.remoteSource.datasources

import androidx.paging.PagingData
import com.erpnext.pos.base.networkBoundResourcePaged
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.mapper.toEntity
import kotlinx.coroutines.flow.Flow

class InventoryRemoteSource(
    private val apiService: APIService,
    private val itemDao: ItemDao
) {

    fun getItems(
        warehouseId: String? = null,
        priceList: String? = null,
        query: String? = null
    ): Flow<PagingData<ItemEntity>> = networkBoundResourcePaged(
        // 🔹 Fuente local: Room (PagingSource)
        query = {
            if (query.isNullOrEmpty()) itemDao.getAllItems()
            else itemDao.getAllFiltered(query)
        },
        // 🔹 Fetch remoto: API REST
        fetch = { page, pageSize ->
            apiService.getInventoryForWarehouse(
                warehouse = warehouseId,
                priceList = priceList,
                offset = page * pageSize,
                limit = pageSize,
            )
        },
        // 🔹 Guardar en cache local
        saveFetchResult = { dtos ->
            val entities = dtos.map { it.toEntity() }
            itemDao.addItems(entities)
        },
        // 🔹 Limpieza previa (solo si es refresh total)
        clearLocalData = {
            if (query.isNullOrEmpty()) itemDao.deleteAll()
        }
    )

    suspend fun getItemDetail(itemId: String): ItemDto {
        return apiService.getItemDetail(itemId)
    }

    suspend fun getCategories(): List<CategoryDto> {
        return apiService.getCategories()
    }
}
