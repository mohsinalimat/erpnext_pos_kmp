package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class POSProfileDto(
    @SerialName("name")
    val profileName: String,
    val warehouse: String,
    val country: String,
    @Serializable(with = IntAsBooleanSerializer::class)
    val disabled: Boolean,
    val company: String,
    val currency: String,
)