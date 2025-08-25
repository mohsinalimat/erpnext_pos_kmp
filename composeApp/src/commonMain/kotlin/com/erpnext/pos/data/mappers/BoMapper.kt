package com.erpnext.pos.data.mappers

import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.UserDto

fun UserDto.toBO(): UserBO {
    return UserBO(
        name = this.name,
        phone = this.phone
    )
}