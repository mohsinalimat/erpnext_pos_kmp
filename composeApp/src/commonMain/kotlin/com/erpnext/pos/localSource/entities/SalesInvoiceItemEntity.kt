package com.erpnext.pos.localSource.entities

import androidx.room.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "tabSalesInvoiceItem",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["invoice_name"],
            childColumns = ["parent_invoice"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parent_invoice"]),
        Index(value = ["item_code"])
    ]
)
data class SalesInvoiceItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 🔗 Referencia a la factura
    @ColumnInfo(name = "parent_invoice")
    val parentInvoice: String, // invoice_name del padre

    // 🧩 Identificación del ítem
    @ColumnInfo(name = "item_code")
    val itemCode: String,
    @ColumnInfo(name = "item_name")
    val itemName: String? = null,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "uom")
    val uom: String? = "Unit",
    @ColumnInfo(name = "qty")
    val qty: Double = 1.0,
    @ColumnInfo(name = "rate")
    val rate: Double = 0.0,
    @ColumnInfo(name = "amount")
    val amount: Double = 0.0,

    // 💰 Cuentas y precios
    @ColumnInfo(name = "price_list_rate")
    val priceListRate: Double = 0.0,
    @ColumnInfo(name = "discount_percentage")
    val discountPercentage: Double = 0.0,
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,
    @ColumnInfo(name = "net_rate")
    val netRate: Double = 0.0,
    @ColumnInfo(name = "net_amount")
    val netAmount: Double = 0.0,

    // 🧾 Impuestos individuales (si aplica)
    @ColumnInfo(name = "tax_rate")
    val taxRate: Double = 0.0,
    @ColumnInfo(name = "tax_amount")
    val taxAmount: Double = 0.0,

    // 📦 Almacén y trazabilidad
    @ColumnInfo(name = "warehouse")
    val warehouse: String? = null,
    @ColumnInfo(name = "batch_no")
    val batchNo: String? = null,
    @ColumnInfo(name = "serial_no")
    val serialNo: String? = null,
    @ColumnInfo("income_account")
    val incomeAccount: String? = null,
    @ColumnInfo("cost_center")
    val costCenter: String? = null,

    // ⚙️ Estado y auditoría
    @ColumnInfo(name = "is_return")
    val isReturn: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = Clock.System.now().toEpochMilliseconds()
)
