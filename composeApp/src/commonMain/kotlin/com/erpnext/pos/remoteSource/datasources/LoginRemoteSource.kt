package com.erpnext.pos.remoteSource.datasources

import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.localSource.dao.UserDao
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import com.erpnext.pos.remoteSource.dto.UserDto

import com.erpnext.pos.remoteSource.mapper.toEntity

class LoginRemoteSource(
    private val apiService: APIService,
    //private val userDao: UserDao
) {
    suspend fun login(credentials: CredentialsDto) : UserDto {
        val data = apiService.login(credentials)
        //userDao.addUsers(data.toEntity())

        return data
    }
}