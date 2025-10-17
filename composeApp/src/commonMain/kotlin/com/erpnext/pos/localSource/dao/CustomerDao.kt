package com.erpnext.pos.localSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("SELECT * FROM customers WHERE customerName LIKE '%' || :search || '%' ORDER BY customerName ASC")
    fun getAllFiltered(search: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE territory = :territory AND customerName LIKE '%' || :search || '%' ORDER BY customerName ASC")
    fun getByTerritory(territory: String, search: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name = :name")
    suspend fun getByName(name: String): CustomerEntity?
}