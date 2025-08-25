package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUsers(user: UserEntity)

    @Query("SELECT * FROM users WHERE name = :userId")
    fun getUserInfo(userId: String): UserEntity

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}