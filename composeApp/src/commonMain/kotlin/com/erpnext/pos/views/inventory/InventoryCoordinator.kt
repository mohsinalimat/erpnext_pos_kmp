package com.erpnext.pos.views.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.remoteSource.dto.ItemDto
import org.koin.compose.viewmodel.koinViewModel

class InventoryCoordinator(
    val viewModel: InventoryViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun fetchInventory(): List<ItemDto>? {
        return viewModel.fetchAllItems()
    }

    fun getItemDetails(itemId: String): ItemDto? {
        return viewModel.getItemDetail(itemId)
    }

    fun refresh() {
        viewModel.refresh()
    }

    fun onError(message: String) {
        viewModel.onError(message)
    }
}

@Composable
fun rememberInventoryCoordinator(): InventoryCoordinator {
    val viewModel: InventoryViewModel = koinViewModel()

    return remember(viewModel) {
        InventoryCoordinator(
            viewModel = viewModel
        )
    }
}