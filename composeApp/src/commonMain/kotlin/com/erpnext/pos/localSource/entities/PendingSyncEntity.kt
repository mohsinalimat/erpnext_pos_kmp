package com.erpnext.pos.localSource.entities

import androidx.room.Entity

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    val id: String,
    val entityType: String,
    val payload: String,
    val createdAt: Long
)