package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: UserEntity)

    @Query("SELECT * FROM tabUser WHERE name = :userId")
    suspend fun getUserInfo(userId: String): UserEntity

    @Query("DELETE FROM tabUser")
    suspend fun deleteAll()
}