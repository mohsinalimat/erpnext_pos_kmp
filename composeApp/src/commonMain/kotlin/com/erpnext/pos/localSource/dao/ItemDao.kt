package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.ItemEntity

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(categories: List<ItemEntity>)

    @Query("SELECT * FROM item")
    fun getAllItems(): List<ItemEntity>

    @Query("SELECT * FROM item WHERE name = :itemId")
    fun getCategory(itemId: String): ItemEntity

    @Query("DELETE FROM item")
    suspend fun deleteAll()
}