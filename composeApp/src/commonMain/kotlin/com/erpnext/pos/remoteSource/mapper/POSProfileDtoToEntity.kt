package com.erpnext.pos.remoteSource.mapper

import com.erpnext.pos.localSource.entities.POSProfileEntity
import com.erpnext.pos.localSource.entities.PaymentModesEntity
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.PaymentModesDto
import kotlin.jvm.JvmName

//TODO: Verificar si las propiedades son las correctas y estan todas mapeadas
@JvmName("toEntityPOSProfileDto")
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
        defaultCurrency = this.currency,
        user = ""
    )
}

fun List<PaymentModesDto>.toEntity(): List<PaymentModesEntity> {
    return this.map { it.toEntity() }
}

fun PaymentModesDto.toEntity(): PaymentModesEntity {
    return PaymentModesEntity(
        name = this.name,
        default = this.default,
        modeOfPayment = this.modeOfPayment
    )
}