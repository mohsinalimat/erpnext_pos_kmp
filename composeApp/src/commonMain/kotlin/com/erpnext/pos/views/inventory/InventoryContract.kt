package com.erpnext.pos.views.inventory

import com.erpnext.pos.remoteSource.dto.ItemDto

sealed class InventoryState {
    object Loading : InventoryState()
    object Empty : InventoryState()
    object Success : InventoryState()
    data class Error(val message: String) : InventoryState()
}

data class InventoryAction(
    val fetchAll: () -> List<ItemDto>? = { emptyList() },
    val getDetails: (String) -> ItemDto? = { null },
    val refresh: () -> Unit = {},
    val onError: (error: String) -> Unit = {}
)