package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.POSProfileEntity

@Dao
interface POSProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pos: List<POSProfileEntity>)

    @Query("SELECT * FROM tabPosProfile WHERE profile_name = :profileId")
    suspend fun getPOSProfile(profileId: String): POSProfileEntity

    @Query("DELETE FROM tabposprofile")
    suspend fun deleteAll()
}