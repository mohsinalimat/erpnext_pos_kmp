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
    private val itemDao: ItemDao,
    private val warehouseId: String? = null,
    private val priceList: String? = null,
    private val pageSize: Int = 20,
    private val preserveCacheOnEmptyRefresh: Boolean = true
) : RemoteMediator<Int, ItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ItemEntity>
    ): MediatorResult {
        return withContext(Dispatchers.IO) {
            try {
                // Determine offset using DB count — efficient for append (keeps continuity).
                val offset = when (loadType) {
                    LoadType.REFRESH -> 0
                    LoadType.PREPEND -> return@withContext MediatorResult.Success(
                        endOfPaginationReached = true
                    )

                    LoadType.APPEND -> itemDao.countAll()
                }

                // Defensive check: warehouse required for remote call
                if (warehouseId.isNullOrBlank()) {
                    // No remote fetch; treat as successful but no remote data
                    return@withContext MediatorResult.Success(endOfPaginationReached = true)
                }

                // Remote fetch
                val fetched = apiService.getInventoryForWarehouse(
                    warehouse = warehouseId,
                    priceList = priceList,
                    offset = offset,
                    limit = pageSize
                )

                val entities = fetched.toEntity()
                val endReached = entities.isEmpty() || entities.size < pageSize

                when (loadType) {
                    LoadType.REFRESH -> {
                        if (!preserveCacheOnEmptyRefresh || entities.isNotEmpty()) {
                            itemDao.deleteAll()
                            if (entities.isNotEmpty()) itemDao.addItems(entities)
                        } // else preserve local cache when refresh yields empty (useful for partial offline)
                    }

                    LoadType.APPEND -> {
                        if (entities.isNotEmpty()) itemDao.addItems(entities)
                    }

                    else -> Unit
                }

                MediatorResult.Success(endOfPaginationReached = endReached)
            } catch (e: IOException) {
                MediatorResult.Error(e)
            } catch (e: Exception) {
                MediatorResult.Error(e)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}
