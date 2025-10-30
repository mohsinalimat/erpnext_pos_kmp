package com.erpnext.pos.remoteSource.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.erpnext.pos.localSource.dao.PendingInvoiceDao
import com.erpnext.pos.localSource.entities.PendingSalesInvoiceEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

@OptIn(ExperimentalPagingApi::class)
class InvoiceRemoteMediator(
    private val apiService: APIService,
    private val pendingInvoiceDao: PendingInvoiceDao,
    private val posProfile: String,
    private val pageSize: Int = 20,
    private val preserveCacheOnEmptyRefresh: Boolean = true
) : RemoteMediator<Int, PendingSalesInvoiceEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PendingSalesInvoiceEntity>
    ): MediatorResult {
        return withContext(Dispatchers.IO) {
            try {
                val offset = when (loadType) {
                    LoadType.REFRESH -> 0
                    LoadType.PREPEND -> return@withContext MediatorResult.Success(
                        endOfPaginationReached = true
                    )

                    LoadType.APPEND -> pendingInvoiceDao.countAll()
                }

                val fetched = apiService.getPendingInvoices(
                    posProfile = posProfile,
                    offset = offset,
                    limit = pageSize
                )
                val entities = fetched.toEntity()
                val endReached = entities.isEmpty() || entities.size < pageSize

                when (loadType) {
                    LoadType.REFRESH -> {
                        if (!preserveCacheOnEmptyRefresh || entities.isNotEmpty()) {
                            pendingInvoiceDao.deleteAll()
                            if (entities.isNotEmpty()) pendingInvoiceDao.insertAll(entities)
                        }
                    }

                    LoadType.APPEND -> {
                        if (entities.isNotEmpty()) pendingInvoiceDao.insertAll(entities)
                    }

                    else -> null
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