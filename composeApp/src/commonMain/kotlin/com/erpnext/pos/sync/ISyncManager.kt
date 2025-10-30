package com.erpnext.pos.sync

import com.erpnext.pos.localSource.entities.PendingSyncEntity

interface ISyncManager {
    suspend fun queuePendingSync(entity: PendingSyncEntity)
    suspend fun syncAll()
}