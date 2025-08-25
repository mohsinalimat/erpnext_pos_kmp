package com.erpnext.pos.localSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.erpnext.pos.localSource.converters.Converters
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.dao.UserDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.localSource.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
}
