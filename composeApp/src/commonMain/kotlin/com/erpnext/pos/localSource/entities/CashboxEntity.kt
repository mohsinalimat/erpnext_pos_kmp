package com.erpnext.pos.localSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "tabCashbox",
    indices = [Index(value = ["posProfile", "user"], unique = true)]
)
data class CashboxEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val posProfile: String,
    val company: String,
    val periodStartDate: Long,
    val user: String,
    val status: Boolean,
    val pendingSync: Boolean = true // Flag para sync (true si no synced)
)

@Entity(
    tableName = "balance_details",
    foreignKeys = [
        ForeignKey(
            entity = CashboxEntity::class,
            parentColumns = ["localId"],
            childColumns = ["cashboxId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BalanceDetailsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var cashboxId: Long = 0,
    val modeOfPayment: String,
    val openingAmount: Double
)

data class CashboxWithDetails(
    @Embedded val cashbox: CashboxEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "cashboxId"
    )
    val details: List<BalanceDetailsEntity>
)