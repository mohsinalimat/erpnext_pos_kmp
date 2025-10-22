package com.erpnext.pos.views.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.domain.models.ItemBO
import org.koin.compose.viewmodel.koinViewModel

class InventoryCoordinator(
    val viewModel: InventoryViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun fetchInventory() {
        viewModel.fetchAllItems()
    }

    fun onPrint() {}

    fun onSearchQueryChanged(input: String) {
        viewModel.onSearchQueryChanged(input)
    }

    fun onClearSearch() {}

    fun onCategorySelected(category: String) {
        viewModel.onCategorySelected(category)
    }

    fun getItemDetails(itemId: String) {
        viewModel.getItemDetail(itemId)
    }

    fun onRefresh() {
        viewModel.refresh()
    }

    fun onError(message: String) {
        viewModel.onError(message)
    }

    fun isCashboxOpen() {
    }

    fun onItemClick(item: ItemBO) {}
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