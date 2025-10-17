package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.repositories.ICustomerRepository
import kotlinx.coroutines.flow.Flow

data class CustomerSearchInput(
    val searchQuery: String,
    val territory: String? = null
)

class CustomerSearchUseCase(
    private val repo: ICustomerRepository
) : UseCase<CustomerSearchInput, Flow<List<CustomerBO>>>() {
    override suspend fun useCaseFunction(input: CustomerSearchInput): Flow<List<CustomerBO>> {
        return repo.getCustomersFiltered(input.searchQuery, input.territory)
    }
}