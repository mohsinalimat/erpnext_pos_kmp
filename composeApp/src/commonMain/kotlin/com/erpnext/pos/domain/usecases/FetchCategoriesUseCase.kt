package com.erpnext.pos.domain.usecases

import com.erpnext.pos.base.Resource
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import kotlinx.coroutines.flow.Flow

class FetchCategoriesUseCase(
    private val repo: IInventoryRepository
) : UseCase<Unit?, Flow<Resource<List<CategoryBO>>>>() {
    override suspend fun useCaseFunction(input: Unit?): Flow<Resource<List<CategoryBO>>> {
        return repo.getCategories()
    }
}