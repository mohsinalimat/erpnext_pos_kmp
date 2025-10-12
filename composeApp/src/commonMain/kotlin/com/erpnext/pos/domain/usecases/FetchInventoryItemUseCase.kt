package com.erpnext.pos.domain.usecases

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import kotlinx.coroutines.flow.Flow

data class InventoryInput(val warehouseId: String? = null, val priceList: String? = null)
class FetchInventoryItemUseCase(
    private val repo: IInventoryRepository
) : UseCase<InventoryInput, Flow<PagingData<ItemBO>>>() {
    override suspend fun useCaseFunction(input: InventoryInput): Flow<PagingData<ItemBO>> {
        return repo.getItems(input.warehouseId, input.priceList)
    }
}