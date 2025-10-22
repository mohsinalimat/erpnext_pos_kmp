package com.erpnext.pos.views.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.utils.formatDoubleToString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import dev.materii.pullrefresh.PullRefreshDefaults
import dev.materii.pullrefresh.PullRefreshIndicator
import dev.materii.pullrefresh.pullRefresh
import dev.materii.pullrefresh.rememberPullRefreshState

@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val threshold = with(density) { 50.dp.toPx() }
    val refreshThreshold = with(density) { 60.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (dragOffset > refreshThreshold) {
                            scope.launch { onRefresh() }
                        }
                        dragOffset = 0f
                    },
                    onDrag = { change, _ ->
                        if (!isRefreshing && change.position.y > 0) { // Solo pull down
                            dragOffset += change.position.y
                        }
                    }
                )
            }
    ) {
        // Contenido principal con offset
        Box(
            modifier = Modifier
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .fillMaxSize()
        ) {
            content()
        }

        // Indicador de pull
        if (dragOffset > 0 && !isRefreshing) {
            val progress = (dragOffset / threshold).coerceIn(0f, 1f)
            val indicatorOffset = (dragOffset / 2).roundToInt()

            Box(
                modifier = Modifier
                    .offset(y = indicatorOffset.dp)
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (progress > 0.8f) "Suelta para actualizar..." else "Tira para actualizar",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.BottomCenter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Indicador de refreshing
        if (isRefreshing) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Actualizando...",
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .align(Alignment.BottomCenter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    actions: InventoryAction,
    baseUrl: String = "https://staging.distribuidorareyes.com"
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Estado para pull-to-refresh custom
    var isRefreshing by remember { mutableStateOf(false) }

    val items: LazyPagingItems<ItemBO>? = when (state) {
        is InventoryState.Success -> state.items.collectAsLazyPagingItems()
        else -> null
    }

    // Visibilidad FAB mejorada
    val fabVisible by remember {
        derivedStateOf {
            when (state) {
                is InventoryState.Success -> {
                    items?.itemCount?.let { count ->
                        count > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    } ?: false
                }

                else -> false
            }
        }
    }

    // Sincronizar refreshing con estado
    LaunchedEffect(state) {
        if (state !is InventoryState.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        // actions.isCashboxOpen()
        if (items != null && items.itemCount == 0) {
            actions.onRefresh()
        }
    }

    // Elevación reactiva
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val filterElevation by animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 0.dp,
        animationSpec = tween(300),
        label = "filterElevation"
    )

    // Estados locales con debounce
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos los grupos de artículos") }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(searchQuery) {
        searchJob?.cancel()
        searchJob = launch {
            delay(300)
            actions.onSearchQueryChanged(searchQuery)
        }
    }

    LaunchedEffect(selectedCategory) {
        actions.onCategorySelected(selectedCategory)
    }

    val pullToRefreshState = rememberPullRefreshState(
        refreshing = state is InventoryState.Loading,
        onRefresh = { actions.onRefresh() },
        refreshThreshold = PullRefreshDefaults.RefreshThreshold,
        refreshingOffset = PullRefreshDefaults.RefreshingOffset,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Inventario", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = {
                        isRefreshing = true
                        actions.onRefresh()
                    }, enabled = state !is InventoryState.Loading) {
                        val infiniteTransition = rememberInfiniteTransition(label = "refresh")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = if (state is InventoryState.Loading) 360f else 0f,
                            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
                            label = "rotation"
                        )
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.OnlinePrediction, contentDescription = "Predicciones")
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = slideInVertically(initialOffsetY = { it }) + scaleIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + scaleOut()
            ) {
                when (state) {
                    is InventoryState.Success -> {
                        val items = state.items.collectAsLazyPagingItems()
                        if (items.itemCount > 0) {
                            ExtendedFloatingActionButton(
                                onClick = { actions.onPrint() },
                                icon = {
                                    Icon(
                                        Icons.Filled.Print,
                                        contentDescription = "Imprimir"
                                    )
                                },
                                text = { Text("Imprimir Lista") },
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullToRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filtros
                AnimatedVisibility(
                    visible = state is InventoryState.Success,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (state is InventoryState.Success) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            tonalElevation = filterElevation,
                            shadowElevation = filterElevation,
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ) {
                            InventoryFilters(
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                categories = state.categories.map { it.name },
                                onQueryChange = { query -> searchQuery = query },
                                onCategoryChange = { category -> selectedCategory = category },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Contenido principal
                when (val currentState = state) {
                    InventoryState.Loading -> FullScreenShimmerLoading()
                    is InventoryState.Error -> FullScreenErrorMessage(
                        errorMessage = currentState.message,
                        onRetry = { actions.onRefresh() }
                    )

                    InventoryState.Empty -> EmptyStateMessage(
                        message = "El inventario está vacío. ¡Agrega productos!",
                        icon = Icons.Filled.Add
                    )

                    is InventoryState.Success -> {
                        val items = currentState.items.collectAsLazyPagingItems()
                        /*InventoryList(
                            items = items,
                            listState = listState,
                            baseUrl = baseUrl,
                            actions = actions,
                            modifier = Modifier.fillMaxSize()
                        )*/
                        if (items.itemCount == 0) {
                            EmptyStateMessage(
                                message = if (searchQuery.isNotEmpty() || selectedCategory != "Todos")
                                    "No hay productos que coincidan con tu búsqueda."
                                else "No hay productos disponibles.",
                                icon = Icons.Filled.SearchOff
                            )
                        } else {
                            InventoryList(
                                items = items,
                                listState = listState,
                                baseUrl = baseUrl,
                                actions = actions,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state is InventoryState.Loading,
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = if (state is InventoryState.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun InventoryFilters(
    searchQuery: String,
    selectedCategory: String,
    categories: List<String>,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (categories.isNotEmpty()) {
            val infiniteTransition = rememberInfiniteTransition(label = "chipTransition")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(2000, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "chipAlpha"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            ) {
                items(categories.reversed()) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category.lowercase().replaceFirstChar { it.titlecase() }) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = FilterChipDefaults.elevatedFilterChipElevation()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        SearchTextField(
            searchQuery = searchQuery,
            onSearchQueryChange = onQueryChange,
            placeholderText = "Buscar por nombre, código o descripcion..."
        )
    }
}

@Composable
fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar...",
    onSearchAction: (() -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        placeholder = { Text(placeholderText, overflow = TextOverflow.Ellipsis, softWrap = false) },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.outline
            )
        },
        trailingIcon = {
            AnimatedVisibility(searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchAction?.invoke()
                keyboardController?.hide()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun InventoryList(
    items: LazyPagingItems<ItemBO>,
    listState: LazyListState,
    baseUrl: String,
    actions: InventoryAction,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.itemCode },
            contentType = items.itemContentType { "item" }
        ) { index ->
            val item = items[index]
            if (item != null) {
                ProductCard(actions, item, baseUrl)
            } else {
                ShimmerProductPlaceholder()
            }
        }

        when (items.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = "Error al cargar más items",
                        onRetry = { items.retry() }
                    )
                }
            }

            else -> {}
        }

        when (items.loadState.refresh) {
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = "Error al cargar inventario",
                        onRetry = { items.retry() }
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
fun ProductCard(actions: InventoryAction, product: ItemBO, baseUrl: String) {
    val formattedPrice by remember(product.price) {
        derivedStateOf {
            formatDoubleToString(
                product.price,
                2
            )
        }
    }
    val formattedQty by remember(product.actualQty) {
        derivedStateOf {
            formatDoubleToString(
                product.actualQty,
                0
            )
        }
    }

    val context = LocalPlatformContext.current
    val imageRequest = remember(product.image) {
        ImageRequest.Builder(context)
            .data("$baseUrl${product.image}")
            .crossfade(300)
            .build()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = { /*actions.onItemClick(product)*/ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Usando SubcomposeAsyncImage para placeholders composables
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = product.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    ShimmerPlaceholder(Modifier.fillMaxSize())
                },
                error = {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error de imagen",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = "Disponible: $formattedQty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.actualQty > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Código: ${product.itemCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "C$ $formattedPrice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.actualQty <= 0) {
                    Text(
                        text = "Agotado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerProductPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shimmerBackground(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                        .shimmerBackground()
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .shimmerBackground()
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .shimmerBackground()
                )
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
                    .shimmerBackground()
            )
        }
    }
}

@Composable
private fun FullScreenShimmerLoading() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(6) {
            ShimmerProductPlaceholder()
        }
    }
}

@Composable
private fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onRetry) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Reintentar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun FullScreenErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.CloudOff,
                contentDescription = "Error",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun Modifier.shimmerBackground(
    shape: CornerBasedShape = RoundedCornerShape(4.dp)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val gradient = Brush.linearGradient(
        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        0.5f to MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        start = Offset(startOffsetX, 0f),
        end = Offset(startOffsetX + 1000f, 0f)
    )

    background(gradient, shape)
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.shimmerBackground())
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Success(
                flowOf(
                    PagingData.from(
                        listOf(
                            ItemBO(
                                "Producto 1",
                                "Desc 1",
                                "CODE1",
                                "",
                                "",
                                "Cat1",
                                "Brand1",
                                100.0,
                                10.0,
                                0.0,
                                false,
                                true,
                                "UND"
                            )
                        )
                    )
                ),
                listOf(CategoryBO("Cat1"))
            ),
            actions = InventoryAction(),
            baseUrl = "https://example.com"
        )
    }
}

// Otros previews similares...
@Preview
@Composable
fun ProductCardPreview() {
    MaterialTheme {
        ProductCard(
            actions = InventoryAction(),
            product = ItemBO(
                "Salchichon c/chile 200g",
                "Salchichon c/chile 200g",
                "SCC200",
                "",
                "https://erp-ni.distribuidorareyes.com/files/355047-1200-900.jpg",
                "EMBUTIDOS",
                "Zurqui",
                15.0,
                200.0,
                0.0,
                false,
                true,
                "Libra"
            ),
            baseUrl = "https://example.com"
        )
    }
}