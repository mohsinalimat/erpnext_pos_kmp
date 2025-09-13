package com.erpnext.pos.remoteSource.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

@OptIn(ExperimentalPagingApi::class)
class InventoryRemoteMediator(
    private val apiService: APIService,
    private val warehouseId: String,
    private val itemDao: ItemDao,
    private val pageSize: Int = 20,
    /**
     * Si true y un REFRESH devuelve lista vacía, preservamos el cache local (no borramos).
     * Si false, borramos siempre la cache en REFRESH.
     */
    private val preserveCacheOnEmptyRefresh: Boolean = true
) : RemoteMediator<Int, ItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ItemEntity>
    ): MediatorResult = withContext(Dispatchers.IO) {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    return@withContext MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val countInDb = itemDao.countAll()
                    println("RemoteMediator: APPEND - countInDb=$countInDb (usado como limit_start)")
                    countInDb
                }
            }

            val itemsDto = apiService.items(
                warehouseId,
                offset = offset,
                limit = pageSize
            )

            val entities = itemsDto.toEntity()
            val endOfPaginationReached = entities.isEmpty() || entities.size < pageSize

            when (loadType) {
                LoadType.REFRESH -> {
                    if (!preserveCacheOnEmptyRefresh || entities.isNotEmpty()) {
                        itemDao.deleteAll()
                        if (entities.isNotEmpty()) itemDao.addItems(entities)
                    } else {
                        println("RemoteMediator: REFRESH returned empty, preserving local cache")
                    }
                }

                else -> {
                    if (entities.isNotEmpty()) {
                        itemDao.addItems(entities)
                    }
                }
            }

            val totalAfter = itemDao.countAll()
            println("RemoteMediator: loadType=$loadType | offset=$offset | fetched=${entities.size} | totalInDb=$totalAfter")

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            e.printStackTrace()
            MediatorResult.Error(e)
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }
}
