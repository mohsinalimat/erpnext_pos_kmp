package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
open class BaseEntity() {
    @ColumnInfo(name = "created_at")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()

    @ColumnInfo(name = "synced")
    val synced: Boolean = false

    @ColumnInfo(name = "last_attempt")
    val lastAttempt: Long? = null

    @ColumnInfo(name = "attempts")
    val attempts: Int = 0
}
