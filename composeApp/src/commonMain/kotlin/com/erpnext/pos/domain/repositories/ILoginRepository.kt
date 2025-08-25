package com.erpnext.pos.domain.repositories

import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.CredentialsDto

interface ILoginRepository {
    suspend fun login(credentials: CredentialsDto) : UserBO
}