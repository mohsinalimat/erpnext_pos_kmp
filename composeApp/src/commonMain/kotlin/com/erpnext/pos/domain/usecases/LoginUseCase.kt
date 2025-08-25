package com.erpnext.pos.domain.usecases

import com.erpnext.pos.data.repositories.LoginRepositories
import com.erpnext.pos.domain.base.UseCase
import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.CredentialsDto

class LoginUseCase(
    private val loginRepositories: LoginRepositories
) : UseCase<CredentialsDto, UserBO>() {
    override suspend fun useCaseFunction(input: CredentialsDto): UserBO {
        return loginRepositories.login(input)
    }
}