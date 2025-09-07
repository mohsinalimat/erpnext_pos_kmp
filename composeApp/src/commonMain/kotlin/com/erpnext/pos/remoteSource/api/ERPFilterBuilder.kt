package com.erpnext.pos.remoteSource.api

class ERPFilterBuilder {
    private val conditions = mutableListOf<List<Any>>()

    fun eq(field: String, value: Any) {
        conditions.add(listOf(field, "=", value))
    }

    fun like(field: String, value: Any) {
        conditions.add(listOf(field, "like", value))
    }

    fun gt(field: String, value: Any) {
        conditions.add(listOf(field, ">", value))
    }

    fun gte(field: String, value: Any) {
        conditions.add(listOf(field, ">=", value))
    }

    fun lt(field: String, value: Any) {
        conditions.add(listOf(field, "<", value))
    }

    fun lte(field: String, value: Any) {
        conditions.add(listOf(field, "<=", value))
    }

    fun build(): List<List<Any>> = conditions
}

fun filters(block: ERPFilterBuilder.() -> Unit): List<List<Any>> {
    return ERPFilterBuilder().apply(block).build()
}
