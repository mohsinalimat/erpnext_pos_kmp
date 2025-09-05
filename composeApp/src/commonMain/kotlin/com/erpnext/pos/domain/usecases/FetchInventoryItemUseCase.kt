package com.erpnext.pos.domain.usecases

import com.erpnext.pos.data.repositories.InventoryRepository
import com.erpnext.pos.domain.models.ItemBO

class FetchInventoryItemUseCase(
    private val repo: InventoryRepository
) : UseCase<Unit, List<ItemBO>>() {
    override suspend fun useCaseFunction(input: Unit): List<ItemBO>{
        return repo.getItems()
    }
}