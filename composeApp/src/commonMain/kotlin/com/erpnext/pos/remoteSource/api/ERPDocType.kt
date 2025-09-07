package com.erpnext.pos.remoteSource.api

data class DocTypeFields(val doctype: ERPDocType, val fields: List<String>)

enum class ERPDocType(val path: String) {
    Item("Item"),
    Bin("Bin"),
    Customer("Customer"),
    SalesInvoice("Sales Invoice"),
    PurchaseInvoice("Purchase Invoice"),
    StockEntry("Stock Entry")
}

val fields: List<DocTypeFields> = listOf(
    DocTypeFields(
        ERPDocType.Item, listOf(
            "item_code",
            "description",
            "item_name",
            "brand",
            "image",
            "item_code",
            "disabled",
            "barcodes",
            "stock_uom"
        )
    )
)

fun ERPDocType.getFields(): List<String> {
    val f = fields.first { it.doctype.path == this.path }
    return f.fields
}