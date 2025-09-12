package com.erpnext.pos.domain.models

data class POSProfileBO(
    val name: String = "",
    val warehouse: String = "",
    val country: String = "",
    val disabled: Boolean = false,
    val company: String = "",
    val currency: String = ""
)