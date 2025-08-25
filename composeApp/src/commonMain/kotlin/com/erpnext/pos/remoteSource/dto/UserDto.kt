package com.erpnext.pos.remoteSource.dto

data class UserDto(
    val name: String,
    val email: String,
    val phone: String,
    val salt: String,
    val hash: String,
    val isActive: Boolean
)