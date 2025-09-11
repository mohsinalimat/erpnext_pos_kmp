package com.erpnext.pos.localSource.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.ItemEntity

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItems(items: List<ItemEntity>)

    @Query("SELECT * FROM tabItem ORDER BY name ASC")
    fun getAllItems(): PagingSource<Int, ItemEntity>

    @Query("SELECT * FROM tabItem WHERE name = :itemId")
    fun getCategory(itemId: String): ItemEntity

    @Query("SELECT COUNT(*) FROM tabItem")
    suspend fun countAll(): Int

    @Query("DELETE FROM tabItem")
    suspend fun deleteAll()
}