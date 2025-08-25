package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.localSource.entities.UserEntity
import com.erpnext.pos.remoteSource.dto.UserDto

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        name = this.name,
        phone = this.phone,
        email = this.email,
        isActive = this.isActive
    )
}