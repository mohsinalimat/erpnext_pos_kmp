package com.erpnext.pos.views.inventory

import androidx.paging.PagingData
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.remoteSource.dto.ItemDto
import kotlinx.coroutines.flow.Flow

sealed class InventoryState {
    object Loading : InventoryState()
    object Empty : InventoryState()
    data class Success(val items: Flow<PagingData<ItemBO>>) : InventoryState()
    data class Error(val message: String) : InventoryState()
}

data class InventoryAction(
    val onCategorySelected: (String) -> Unit = {},
    val onSearchQueryChanged: (String) -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onPrint: () -> Unit = {},
    val onClearSearch: () -> Unit = {},
    val fetchAll: () -> Unit = { },
    val getDetails: (String) -> Unit = { },
    val filter: (String, String) -> Unit = { _, _ -> },
    val onError: (error: String) -> Unit = {}
)