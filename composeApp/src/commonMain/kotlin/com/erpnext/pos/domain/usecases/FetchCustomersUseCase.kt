package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.repositories.ICustomerRepository
import kotlinx.coroutines.flow.Flow

data class CustomerFilterInput(val search: String?, val territory: String?)
class FetchCustomersUseCase(
    private val repo: ICustomerRepository
) : UseCase<CustomerFilterInput, Flow<List<CustomerBO>>>() {
    override suspend fun useCaseFunction(input: CustomerFilterInput): Flow<List<CustomerBO>> {
        return repo.getCustomers(input.territory, input.search)
    }
}