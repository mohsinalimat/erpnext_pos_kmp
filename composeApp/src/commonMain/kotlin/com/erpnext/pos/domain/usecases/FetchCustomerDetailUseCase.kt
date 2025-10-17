package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.repositories.ICustomerRepository

class FetchCustomerDetailUseCase(
    private val repo: ICustomerRepository
) : UseCase<String, CustomerBO?>() {
    override suspend fun useCaseFunction(input: String): CustomerBO? {
        return repo.getCustomerByName(input)
    }
}