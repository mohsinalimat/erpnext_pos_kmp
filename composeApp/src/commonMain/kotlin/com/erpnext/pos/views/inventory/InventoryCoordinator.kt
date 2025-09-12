package com.erpnext.pos.views.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.remoteSource.dto.ItemDto
import org.koin.compose.viewmodel.koinViewModel

class InventoryCoordinator(
    val viewModel: InventoryViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun fetchInventory() {
        viewModel.fetchAllItems()
    }

    fun onPrint() {}

    fun onSearchQueryChanged(input: String) {}

    fun onClearSearch() {}

    fun onCategorySelected(category: String) {}

    fun getItemDetails(itemId: String) {
        viewModel.getItemDetail(itemId)
    }

    fun filter(category: String, query: String) {
        viewModel.filter(category, query)
    }

    fun onRefresh() {
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