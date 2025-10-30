package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erpnext.pos.localSource.entities.PendingSyncEntity

@Dao
interface PendingSyncDao {
    @Insert
    suspend fun insert(item: PendingSyncEntity)
    @Query("SELECT * FROM pending_sync")
    suspend fun getAllPending(): List<PendingSyncEntity>
    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun delete(id: String)
}