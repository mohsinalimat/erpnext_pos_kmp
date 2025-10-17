package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.repositories.ICustomerRepository

class FetchCustomersUseCase(
    private val repo: ICustomerRepository
) : UseCase<Unit?, Unit>() {
    override suspend fun useCaseFunction(input: Unit?) {
        repo.getCustomers()
    }
}