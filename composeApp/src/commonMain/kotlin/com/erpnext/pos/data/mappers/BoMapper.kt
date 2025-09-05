package com.erpnext.pos.data.mappers

import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.UserDto

fun UserDto.toBO(): UserBO {
    return UserBO(
        name = this.name,
        phone = this.mobileNo,
    )
}

fun List<ItemDto>.toBO(): List<ItemBO> {
    return this.map { it.toBO() }
}

fun ItemDto.toBO(): ItemBO {
    return ItemBO(
        name = this.name,
        uom = this.uom,
        image = this.image,
        price = this.price,
        discount = this.discount,
        barcode = this.barcode,
        isService = this.isService,
        isStocked = this.isStocked,
        description = this.description
    )
}