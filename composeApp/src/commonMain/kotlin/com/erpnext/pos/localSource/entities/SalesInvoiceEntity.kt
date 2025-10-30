package com.erpnext.pos.localSource.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "tabSalesInvoice",
    foreignKeys = [
        ForeignKey(
            entity = POSProfileEntity::class,
            parentColumns = ["profile_name"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["customer"]),
        Index(value = ["status"]),
        Index(value = ["posting_date"])
    ]
)
data class SalesInvoiceEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 🔗 Relación con el perfil POS
    @ColumnInfo(name = "profile_id")
    val profileId: String? = null,

    // 🧾 Identificación contable
    @ColumnInfo(name = "invoice_name")
    val invoiceName: String? = null, // Nombre remoto (ej. SINV-00045)
    @ColumnInfo(name = "reference_no")
    val referenceNo: String? = null, // Ej. número de recibo o comprobante externo

    // 👤 Cliente y compañía
    val customer: String,
    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,
    val company: String,
    val branch: String? = null,
    val warehouse: String? = null,
    val currency: String = "USD",

    // 📅 Fechas clave
    @ColumnInfo(name = "posting_date")
    val postingDate: String, // Fecha de emisión
    @ColumnInfo(name = "due_date")
    val dueDate: String? = null, // Fecha límite de pago
    @ColumnInfo(name = "posting_time")
    val postingTime: String? = null, // Hora precisa (opcional)

    // 💰 Montos y cuentas
    @ColumnInfo(name = "net_total")
    val netTotal: Double = 0.0, // Subtotal sin impuestos
    @ColumnInfo(name = "tax_total")
    val taxTotal: Double = 0.0, // Total de impuestos
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,
    @ColumnInfo(name = "grand_total")
    val grandTotal: Double = 0.0, // Total final con impuestos
    @ColumnInfo(name = "paid_amount")
    val paidAmount: Double = 0.0,
    @ColumnInfo(name = "outstanding_amount")
    val outstandingAmount: Double = 0.0, // Saldo pendiente

    // 📦 Contabilidad
    @ColumnInfo(name = "income_account")
    val incomeAccount: String? = null,
    @ColumnInfo(name = "expense_account")
    val expenseAccount: String? = null,
    @ColumnInfo(name = "cost_center")
    val costCenter: String? = null,
    @ColumnInfo(name = "price_list")
    val priceList: String? = null,

    // ⚙️ Estado y sincronización
    val status: String = "Draft", // Draft, Submitted, Paid, Cancelled
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "Pending", // Pending, Synced, Failed
    @ColumnInfo(name = "docstatus")
    val docstatus: Int = 0, // 0 = borrador, 1 = enviado, 2 = cancelado
    @ColumnInfo(name = "is_return")
    val isReturn: Boolean = false, // Nota de crédito o devolución

    // 💳 Método de pago (último o principal)
    @ColumnInfo(name = "mode_of_payment")
    val modeOfPayment: String? = null,

    // 🧠 Auditoría y metadatos
    @ColumnInfo(name = "created_by")
    val createdBy: String? = null,
    @ColumnInfo(name = "modified_by")
    val modifiedBy: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = Clock.System.now().toEpochMilliseconds(),
    @ColumnInfo(name = "remarks")
    val remarks: String? = null
)


data class SalesInvoiceWithItemsAndPayments(

    @Embedded
    val invoice: SalesInvoiceEntity,

    @Relation(
        parentColumn = "invoice_name",
        entityColumn = "parent_invoice"
    )
    val items: List<SalesInvoiceItemEntity> = emptyList(),

    @Relation(
        parentColumn = "invoice_name",
        entityColumn = "parent_invoice"
    )
    val payments: List<POSInvoicePaymentEntity> = emptyList()
)