package com.erpnext.pos.domain.usecases

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import kotlinx.coroutines.flow.Flow

class FetchInventoryItemUseCase(
    private val repo: IInventoryRepository
) : UseCase<String, Flow<PagingData<ItemBO>>>() {
    override suspend fun useCaseFunction(input: String): Flow<PagingData<ItemBO>> {
        return repo.getItems(input)
    }
}