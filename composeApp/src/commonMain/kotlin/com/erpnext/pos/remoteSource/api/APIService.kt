package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.localSource.dao.UserDao
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import com.erpnext.pos.remoteSource.dto.UserDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import io.ktor.http.cio.Response

interface APIService {

    @POST("login")
    suspend fun login(@Body() credentials: CredentialsDto) : UserDto
}