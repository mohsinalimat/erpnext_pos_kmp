package com.erpnext.pos.remoteSource.sdk

data class DocTypeFields(val doctype: ERPDocType, val fields: List<String>)

enum class ERPDocType(val path: String) {
    Item("Item"),
    Category("Item Group"),
    ItemPrice("Item Price"),
    User("User"),
    Bin("Bin"),
    Customer("Customer"),
    SalesInvoice("Sales Invoice"),
    PurchaseInvoice("Purchase Invoice"),
    StockEntry("Stock Entry"),
    POSProfile("POS Profile"),
    POSProfileEntry("POS Profile Entry")
}

val fields: List<DocTypeFields> = listOf(
    DocTypeFields(
        ERPDocType.Item,
        listOf(
            "item_code",
            "description",
            "item_name",
            "brand",
            "image",
            "item_code",
            "disabled",
            "barcodes",
            "stock_uom",
            "item_group"
        ),
    ),
    DocTypeFields(
        ERPDocType.Category, listOf("name")
    ),
    DocTypeFields(
        ERPDocType.POSProfile, listOf(
            "name", "company",
            "customer", "disabled", "currency", "warehouse", "country"
        )
    ),
    DocTypeFields(
        ERPDocType.User,
        listOf(
            "name",
            "first_name",
            "last_name",
            "username",
            "language",
            "full_name"
        )
    )
)

fun ERPDocType.getFields(): List<String> {
    val f = fields.first { it.doctype.path == this.path }
    return f.fields
}