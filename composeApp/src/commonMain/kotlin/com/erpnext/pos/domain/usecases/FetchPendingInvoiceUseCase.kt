package com.erpnext.pos.domain.usecases

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.domain.repositories.ISaleInvoiceRepository
import kotlinx.coroutines.flow.Flow

data class PendingInvoiceInput(
    val pos: String,
    val query: String? = null,
    val date: String? = null,
)

class FetchPendingInvoiceUseCase(
    private val repo: ISaleInvoiceRepository
) : UseCase<PendingInvoiceInput, Flow<PagingData<PendingInvoiceBO>>>() {
    override suspend fun useCaseFunction(input: PendingInvoiceInput): Flow<PagingData<PendingInvoiceBO>> {
        return repo.getPendingInvoices(input)
    }
}