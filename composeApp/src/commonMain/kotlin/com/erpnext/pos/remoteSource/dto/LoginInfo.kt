package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginInfo(
    val url: String,
    val redirectUrl: String,
    val clientId: String,
    val clientSecret: String,
    val scopes: List<String>,
    val name: String
)