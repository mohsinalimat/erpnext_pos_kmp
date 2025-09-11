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
                    // ERPNext no soporta prepend hacia atrás
                    return@withContext MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // ← aquí está el cambio crítico:
                    // No uses `state.pages.sumOf { it.data.size }` — usa el conteo real en DB
                    val countInDb = itemDao.countAll()
                    // Debug log
                    println("RemoteMediator: APPEND - countInDb=$countInDb (usado como limit_start)")
                    countInDb
                }
            }

            // Llamada a ERPNext con limit_start = offset y limit_page_length = pageSize
            val itemsDto = apiService.items(
                offset = offset,
                limit = pageSize
            )

            // Map DTO -> Entities (asegurate que toEntity() convierte LISTA completa)
            val entities = itemsDto.toEntity()

            // Consideramos que llegamos al final si la lista devuelta es vacía o menor que pageSize
            val endOfPaginationReached = entities.isEmpty() || entities.size < pageSize

            // Guardado inteligente en DB
            when (loadType) {
                LoadType.REFRESH -> {
                    if (!preserveCacheOnEmptyRefresh || entities.isNotEmpty()) {
                        // Reemplazamos la cache (clear + insert) cuando:
                        //  - no queremos preservar cache si empty, o
                        //  - la API devolvió algo (insertamos)
                        itemDao.deleteAll()
                        if (entities.isNotEmpty()) itemDao.addItems(entities)
                    } else {
                        // preserveCacheOnEmptyRefresh == true && entities.isEmpty() -> mantenemos cache
                        println("RemoteMediator: REFRESH returned empty, preserving local cache")
                    }
                }
                else -> {
                    // APPEND -> insert incremental
                    if (entities.isNotEmpty()) {
                        itemDao.addItems(entities)
                    }
                }
            }

            // Debug final: cuantos hay ahora
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
