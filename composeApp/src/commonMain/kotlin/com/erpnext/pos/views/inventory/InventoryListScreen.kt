package com.erpnext.pos.views.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.erpnext.pos.views.inventory.components.EmptyStateMessage
import com.erpnext.pos.views.inventory.components.FullScreenErrorMessage
import com.erpnext.pos.views.inventory.components.FullScreenShimmerLoading
import com.erpnext.pos.views.inventory.components.InventoryContent
import com.erpnext.pos.views.inventory.components.InventoryTopBar
import dev.materii.pullrefresh.PullRefreshIndicator
import dev.materii.pullrefresh.pullRefresh
import dev.materii.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    actions: InventoryAction,
) {
    val listState = rememberLazyListState()

    // collectAsLazyPagingItems only once per composition when state is Success
    val itemsLazy = (state as? InventoryState.Success)
        ?.items
        ?.collectAsLazyPagingItems()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state is InventoryState.Loading,
        onRefresh = { actions.onRefresh() }
    )

    val fabVisible by remember {
        derivedStateOf {
            val count = itemsLazy?.itemCount ?: 0
            count > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos los grupos de artículos") }

    Scaffold(
        topBar = {
            InventoryTopBar(
                onRefresh = actions.onRefresh,
                isLoading = state is InventoryState.Loading
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = fabVisible, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(onClick = actions.onPrint) {
                    Icon(Icons.Filled.Print, contentDescription = "Imprimir")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            when (state) {
                InventoryState.Loading -> FullScreenShimmerLoading()
                is InventoryState.Error -> FullScreenErrorMessage(state.message) { actions.onRefresh() }
                InventoryState.Empty -> EmptyStateMessage(
                    "Inventario vacío",
                    Icons.Default.Inventory2
                )

                is InventoryState.Success -> {
                    InventoryContent(
                        state = state,
                        itemsLazy = itemsLazy,
                        listState = listState,
                        actions = actions,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onQueryChanged = { query ->
                            searchQuery = query
                            actions.onSearchQueryChanged(query)
                        },
                        onCategorySelected = { category ->
                            selectedCategory = category
                            actions.onCategorySelected(category)
                        }
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = state is InventoryState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
@Preview
fun InventoryScreenPreview() {
    InventoryScreen(
        state = InventoryState.Success(
            items = flowOf(PagingData.empty()),
            categories = emptyList()
        ),
        actions = InventoryAction(),
    )
}
