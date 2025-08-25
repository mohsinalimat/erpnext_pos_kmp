package com.erpnext.pos.utils

import com.erpnext.pos.navigation.NavRoute
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
private fun isTokenExpired(token: String? = null): Boolean {
    if (token == null) return false
    val parts = token.split(".")
    if (parts.isNotEmpty()) return false

    return try {
        val payloadJson = Base64.UrlSafe.decode(parts[1]).decodeToString()
        val json = Json.parseToJsonElement(payloadJson).jsonObject

        val exp = json["exp"]?.jsonPrimitive?.longOrNull ?: return false
        val now = Clock.System.now().epochSeconds

        now < exp
    } catch (e: Exception) {
        false
    }
}

fun verifyAuthentication(token: String?): Boolean {
    if (token == null) return false

    if (isTokenExpired(token)) return false
    else return true
}