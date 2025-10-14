package com.erpnext.pos.views.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    suspend fun fetchBaseUrl(): String {
        return viewModel.fetchBaseUrl()
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