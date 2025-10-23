package com.erpnext.pos.views.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.views.inventory.InventoryAction

@Composable
fun InventoryList(
    items: LazyPagingItems<ItemBO>,
    listState: LazyListState,
    actions: InventoryAction,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = items.itemCount,
            key = { index ->
                val item = items[index]
                // Usa una propiedad estable como clave
                item?.itemCode ?: item?.name ?: index
            }
        ) { index ->
            val item = items[index]
            if (item != null) {
                ProductCard(actions, item)
            } else {
                ShimmerProductPlaceholder()
            }
        }

        when (items.loadState.append) {
            is LoadState.Loading -> {
                item { LoadingMoreItem() }
            }

            is LoadState.Error -> {
                item {
                    ErrorItem("Error al cargar más items", onRetry = { items.retry() })
                }
            }

            else -> {}
        }

        when (items.loadState.refresh) {
            is LoadState.Error -> {
                item {
                    ErrorItem("Error al cargar inventario", onRetry = { items.retry() })
                }
            }

            else -> {}
        }
    }
}

@Composable
fun LoadingMoreItem() {
    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(24.dp))
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reintentar"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Reintentar")
        }
    }
}
