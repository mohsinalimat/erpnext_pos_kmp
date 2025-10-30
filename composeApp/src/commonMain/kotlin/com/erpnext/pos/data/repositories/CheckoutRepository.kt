package com.erpnext.pos.data.repositories

import com.erpnext.pos.localSource.entities.PendingSalesInvoiceEntity

interface ICheckoutRepository {
    suspend fun insertInvoice(entity: PendingSalesInvoiceEntity): Long
    suspend fun markAsSynced(id: Long)
    suspend fun markAsFailed(id: Long, lastAttempt: Long)
    suspend fun countPending(): Int
}

class CheckoutRepository(
    //private val remoteSource: CheckoutRemoteSource,
    //private val localSource: CheckoutLocalSource
) : ICheckoutRepository {
}