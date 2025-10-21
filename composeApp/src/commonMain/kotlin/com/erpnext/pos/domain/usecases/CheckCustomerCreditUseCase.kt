package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.repositories.ICustomerRepository

data class CustomerCreditInput(
    val customerId: String,
    val amount: Double
)

class CheckCustomerCreditUseCase(
    private val repo: ICustomerRepository
) : UseCase<CustomerCreditInput, Boolean>() {

    override suspend fun useCaseFunction(input: CustomerCreditInput): Boolean {
        val customer = repo.getCustomerByName(input.customerId) ?: return false
        val totalSale = input.amount
        val totalPending = customer.totalPendingAmount
        return (customer.availableCredit - totalPending >= totalSale)
    }
}