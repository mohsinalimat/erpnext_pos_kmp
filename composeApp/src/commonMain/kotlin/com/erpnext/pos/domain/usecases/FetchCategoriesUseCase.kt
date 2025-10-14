package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.repositories.IInventoryRepository

class FetchCategoriesUseCase(
    private val repo: IInventoryRepository
) : UseCase<Unit?, List<CategoryBO>>() {
    override suspend fun useCaseFunction(input: Unit?): List<CategoryBO> {
        return repo.getCategories()
    }
}