package com.erpnext.pos.views.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.views.inventory.InventoryAction
import com.erpnext.pos.views.inventory.InventoryState

@Composable
fun InventoryContent(
    state: InventoryState.Success,
    itemsLazy: LazyPagingItems<ItemBO>?,
    listState: LazyListState,
    actions: InventoryAction,
    searchQuery: String,
    selectedCategory: String,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        InventoryFilters(
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            categories = state.categories?.map { it.name!! },
            onQueryChange = onQueryChanged,
            onCategoryChange = onCategorySelected,
            onSearchQueryChanged = { query -> {} },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        // Inventory list (single collector point)
        if (itemsLazy != null) {
            InventoryList(
                items = itemsLazy,
                listState = listState,
                actions = actions
            )
        } else {
            FullScreenShimmerLoading()
        }
    }
}
