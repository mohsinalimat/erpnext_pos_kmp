package com.erpnext.pos.data.mappers

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.CreditLimit
import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.models.PaymentModesBO
import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.CustomerDto
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.POSProfileSimpleDto
import com.erpnext.pos.remoteSource.dto.PaymentModesDto
import com.erpnext.pos.remoteSource.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.jvm.JvmName

fun Flow<PagingData<ItemEntity>>.toPagingBO(): Flow<PagingData<ItemBO>> {
    return transform { value ->
        emit(value.map {
            it.toBO()
        })
    }
}

@JvmName("toBOPaymentModesDto")
fun List<PaymentModesDto>.toBO(): List<PaymentModesBO> {
    return this.map { it.toBO() }
}

fun PaymentModesDto.toBO(): PaymentModesBO {
    return PaymentModesBO(
        name = this.name,
        modeOfPayment = this.modeOfPayment
    )
}

fun List<POSProfileSimpleDto>.toBO(): List<POSProfileBO> {
    return this.map { it.toBO() }
}

fun POSProfileSimpleDto.toBO(): POSProfileBO {
    return POSProfileBO(
        name = this.profileName,
        warehouse = this.warehouse,
        country = this.country,
        disabled = this.disabled,
        company = this.company,
        currency = this.currency,
        paymentModes = emptyList()
    )
}

fun UserDto.toBO(): UserBO {
    return UserBO(
        name = this.name,
        username = this.username,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        language = this.language,
        enabled = this.enabled
    )
}

@JvmName("toBOItemDto")
fun List<ItemDto>.toBO(): List<ItemBO> {
    return this.map { it.toBO() }
}

fun ItemDto.toBO(): ItemBO {
    return ItemBO(
        name = this.itemName,
        uom = this.stockUom,
        image = this.image,
        brand = this.brand,
        itemGroup = this.itemGroup,
        itemCode = this.itemCode,
        description = this.description
    )
}

fun ItemEntity.toBO(): ItemBO {
    return ItemBO(
        name = this.name,
        actualQty = this.actualQty,
        uom = this.stockUom,
        brand = this.brand,
        itemGroup = this.itemGroup,
        itemCode = this.itemCode,
        image = this.image,
        price = this.price,
        discount = this.discount,
        barcode = this.barcode,
        isService = this.isService,
        isStocked = this.isStocked,
        description = this.description,
    )
}

@JvmName("toBOCategoryDto")
fun List<CategoryDto>.toBO(): List<CategoryBO> {
    return this.map { it.toBO() }
}

fun CategoryDto.toBO(): CategoryBO {
    return CategoryBO(
        name = this.name,
    )
}

@JvmName("toProfileDtoToBO")
fun POSProfileDto.toBO(): POSProfileBO {
    return POSProfileBO(
        name = this.profileName,
        warehouse = this.warehouse,
        country = this.country,
        disabled = this.disabled,
        company = this.company,
        currency = this.currency,
        paymentModes = this.payments.toBO(),
    )
}