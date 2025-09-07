package com.erpnext.pos.remoteSource.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*

suspend inline fun <reified T> HttpClient.getERPList(
    doctype: String,
    fields: List<String> = emptyList(),
    filters: List<List<Any>> = emptyList(),
    limit: Int = 20,
    offset: Int = 0,
    orderBy: String? = null,
    baseUrl: String?
): List<T> {
    if (!baseUrl.isNullOrEmpty()) {
        val params = mutableListOf<String>()

        if (fields.isNotEmpty()) {
            params.add(
                "fields=${
                    fields.joinToString(
                        prefix = "[\"",
                        separator = "\",\"",
                        postfix = "\"]"
                    )
                }"
            )
        }
        if (filters.isNotEmpty()) {
            params.add("filters=${Json.encodeToString(filters)}")
        }
        params.add("limit_page_length=$limit")
        params.add("limit_start=$offset")
        orderBy?.let { params.add("order_by=$it") }

        val url = "$baseUrl/api/resource/$doctype?${params.joinToString("&")}"

        val response: HttpResponse = this.get(url)
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return Json.decodeFromString(json["data"].toString())
    } else {
        throw Exception("Peticion Invalida")
    }
}

suspend inline fun <reified T> HttpClient.getERPSingle(
    doctype: String,
    name: String,
    baseUrl: String
): T {
    val url = "$baseUrl/api/resource/$doctype/$name"
    val response: HttpResponse = this.get(url)
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend inline fun <reified T, reified R> HttpClient.postERP(
    doctype: String,
    payload: T,
    baseUrl: String
): R {
    val url = "$baseUrl/api/resource/$doctype"
    val response: HttpResponse = this.post(url) {
        setBody(Json.encodeToString(payload))
    }
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend inline fun <reified T, reified R> HttpClient.putERP(
    doctype: String,
    name: String,
    payload: T,
    baseUrl: String
): R {
    val url = "$baseUrl/api/resource/$doctype/$name"
    val response: HttpResponse = this.put(url) {
        setBody(Json.encodeToString(payload))
    }
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend fun HttpClient.deleteERP(
    doctype: String,
    name: String,
    apiKey: String,
    apiSecret: String,
    baseUrl: String
) {
    val url = "$baseUrl/api/resource/$doctype/$name"
    this.delete(url) {
        headers { append("Authorization", "token $apiKey:$apiSecret") }
    }
}
