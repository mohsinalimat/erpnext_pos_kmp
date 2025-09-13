package com.erpnext.pos.domain.usecases

import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.repositories.IPOSRepository

class FetchPosProfileUseCase(
    private val repo: IPOSRepository
) : UseCase<Unit?, List<POSProfileBO>>() {
    override suspend fun useCaseFunction(input: Unit?): List<POSProfileBO> {
        return repo.getPOSProfiles()
    }
}