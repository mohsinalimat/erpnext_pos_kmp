package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.localSource.entities.POSProfileEntity
import com.erpnext.pos.remoteSource.dto.POSProfileDto

//TODO: Verificar si las propiedades son las correctas y estan todas mapeadas
fun List<POSProfileDto>.toEntity(): List<POSProfileEntity> {
    return this.map { it.toEntity() }
}

fun POSProfileDto.toEntity(): POSProfileEntity {
    return POSProfileEntity(
        profileName = this.profileName,
        warehouse = this.warehouse,
        country = this.country,
        disabled = this.disabled,
        company = this.company,
        currency = this.currency,
        defaultCurrency = this.currency
    )
}