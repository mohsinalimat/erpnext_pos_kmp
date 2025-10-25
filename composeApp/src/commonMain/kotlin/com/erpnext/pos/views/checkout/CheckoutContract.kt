package com.erpnext.pos.views.checkout

import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.domain.models.ItemBO

sealed class CheckoutState {
    object Loading : CheckoutState()
    object Empty : CheckoutState()
    data class Success(val products: List<ItemBO>, val customers: List<CustomerBO>) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

data class CheckoutAction(
    val checkCredit: (String, Double, (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    val loadItems: () -> Unit = {},
    val onError: (String) -> Unit = {},
    val deleteItem: (String) -> Unit = {},
    val loadCustomers: () -> Unit = {},
    val saveInvoice: () -> Unit = {},
    val checkItemQty: () -> Unit = {},
    val calculateItemTotal: () -> Unit = {},
    val loadPosProfile: () -> Unit = {}
)