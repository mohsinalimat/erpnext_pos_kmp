package com.erpnext.pos.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.dao.POSProfileDao
import com.erpnext.pos.localSource.dao.UserDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.localSource.entities.ModeOfPaymentEntity
import com.erpnext.pos.localSource.entities.POSInvoiceEntity
import com.erpnext.pos.localSource.entities.POSInvoiceItemEntity
import com.erpnext.pos.localSource.entities.POSInvoicePaymentEntity
import com.erpnext.pos.localSource.entities.POSProfileEntity
import com.erpnext.pos.localSource.entities.SalesInvoiceEntity
import com.erpnext.pos.localSource.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class,
        ModeOfPaymentEntity::class,
        POSInvoiceEntity::class,
        POSInvoiceItemEntity::class,
        POSInvoicePaymentEntity::class,
        POSProfileEntity::class,
        SalesInvoiceEntity::class,
    ],
    version = 6,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao

    abstract fun posProfileDao(): POSProfileDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect class DatabaseBuilder {
    fun build(): AppDatabase
}
