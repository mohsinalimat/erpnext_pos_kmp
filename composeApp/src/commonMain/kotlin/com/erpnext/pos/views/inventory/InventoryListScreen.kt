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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.utils.formatDoubleToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    state: InventoryState,
    actions: InventoryAction
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )
    val gridState = rememberLazyGridState()

    var previousFirstVisibleItem by remember { mutableStateOf(0) }
    var isFabReallyVisibleBasedOnScroll by remember { mutableStateOf(true) }

    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.isScrollInProgress) {
        if (!gridState.isScrollInProgress && !isFabReallyVisibleBasedOnScroll) {
            isFabReallyVisibleBasedOnScroll = true
        } else if (gridState.isScrollInProgress) {
            if (gridState.firstVisibleItemIndex > previousFirstVisibleItem) {
                isFabReallyVisibleBasedOnScroll = false
            } else if (gridState.firstVisibleItemIndex < previousFirstVisibleItem) {
                isFabReallyVisibleBasedOnScroll = true
            }
        }
        previousFirstVisibleItem = gridState.firstVisibleItemIndex
    }

    val finalFabVisibility = when (state) {
        is InventoryState.Success -> isFabReallyVisibleBasedOnScroll
        else -> false
    }

    val isContentScrolledUnderFilters by remember {
        derivedStateOf {
            print("InventoryScreen - GridState: firstVisibleItemIndex=${gridState.firstVisibleItemIndex}, firstVisibleItemScrollOffset=${gridState.firstVisibleItemScrollOffset}")
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }

    val filterElevation by animateDpAsState(
        targetValue = if (isContentScrolledUnderFilters) 4.dp else 0.dp,
        label = "filterElevation"
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

   /* LaunchedEffect(Unit) {
        actions.fetchAll()
    }*/

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Inventario", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(
                        onClick = actions.onRefresh,
                        enabled = state != InventoryState.Loading
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            "Actualizar Inventario",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.OnlinePrediction,
                            "Online Prediction",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = finalFabVisibility, // <--- USA LA VARIABLE DE VISIBILIDAD
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
            ) {
                when (state) {
                    is InventoryState.Success -> {
                        if (state.items.collectAsLazyPagingItems().itemCount > 0)
                            ExtendedFloatingActionButton(
                                onClick = actions.onPrint,
                                icon = { Icon(Icons.Filled.Print, "Imprimir lista") },
                                text = { Text("Imprimir") }
                            )
                    }

                    else -> {}
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {


            when (val currentState = state) {
                InventoryState.Loading -> {
                    FullScreenLoadingIndicator()
                }

                is InventoryState.Error -> {
                    FullScreenErrorMessage(
                        errorMessage = currentState.message,
                        onRetry = actions.onRefresh
                    )
                }

                InventoryState.Empty -> { // Estado explícito de "completamente vacío" desde el ViewModel
                    EmptyStateMessage(
                        message = "El inventario está completamente vacío.",
                        icon = Icons.Filled.SearchOff // O un icono más genérico
                    )
                }

                is InventoryState.Success -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = filterElevation,
                        shadowElevation = filterElevation,
                    ) {
                        InventoryFilters(
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            categories = currentState.categories.map { it.name },
                            onQueryChange = { query ->
                                searchQuery = query
                                actions.onSearchQueryChanged(query)
                            },
                            onCategoryChange = { category ->
                                actions.onCategorySelected(category)
                                selectedCategory = category
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                        )
                    }

                    val items = currentState.items.collectAsLazyPagingItems()
                    if (items.itemCount == 0 && items.loadState.refresh !is LoadState.Loading) {
                        EmptyStateMessage(
                            message = "No se encontraron productos que coincidan con tu selección.",
                            icon = Icons.Filled.SearchOff
                        )
                    } else if (items.loadState.refresh is LoadState.Loading && items.itemCount == 0) {
                        FullScreenLoadingIndicator()
                    } else {
                        InventoryListContent(
                            paddingValue = paddingValues,
                            items = items,
                            listState = gridState,
                            actions = actions
                        )
                    }
                }
            }
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
    Column(modifier = modifier) {
        if (categories.isNotEmpty()) { // Solo mostrar si hay categorías
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.reversed()) { category ->
                    val isSelected = category == selectedCategory

                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category.lowercase().replaceFirstChar { it.titlecase() }) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SearchTextField(
            searchQuery = searchQuery,
            onSearchQueryChange = onQueryChange,
            placeholderText = "Buscar por nombre o código..."
        )
    }
}

@Composable
fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Buscar...",
    onSearchAction: (() -> Unit)? = null // Acción opcional al presionar "buscar" en el teclado
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { query -> onSearchQueryChange(query) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Un poco de padding vertical para que respire
        placeholder = { Text(placeholderText, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Icono de búsqueda",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Un color sutil para el icono
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = {
                    onSearchQueryChange("") // Borra la búsqueda
                    keyboardController?.show() // Opcional: vuelve a mostrar el teclado si se desea
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Borrar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = if (onSearchAction != null) ImeAction.Search else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (onSearchAction != null) {
                    onSearchAction()
                    keyboardController?.hide()
                } else {
                    keyboardController?.hide()
                }
            },
            onDone = {
                keyboardController?.hide()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors( // Colores para que se vea más "Material You"
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium // Bordes redondeados consistentes con M3
    )
}

@Composable
private fun InventoryListContent(
    paddingValue: PaddingValues,
    items: LazyPagingItems<ItemBO>,
    listState: LazyGridState,
    actions: InventoryAction
) {
    Box(
        modifier = Modifier//.padding(paddingValue)
            .fillMaxSize()
    ) {
        when (items.loadState.refresh) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        trackColor = Color.Blue,
                        color = Color.Cyan,
                        strokeWidth = 2.dp
                    )
                }
            }

            is LoadState.Error -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValue)
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    Text(text = "Error")
                }
            }

            else -> {
                if (items.itemCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron productos.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = items.itemCount,
                            key = items.itemKey { it.itemCode },
                            contentType = items.itemContentType { it.itemCode }
                        ) { index ->
                            val product = items[index]
                            if (product != null) {
                                ProductRowItem(actions, product)
                            } else {
                                PlaceholderProductRowItem()
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            if (items.loadState.append is LoadState.Loading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                                        trackColor = Color.Blue,
                                        color = Color.Cyan,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        if (items.loadState.append is LoadState.Error) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Error al cargar más.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRowItem(actions: InventoryAction, product: ItemBO) {
    val formattedPrice = remember(product.price) {
        formatDoubleToString(product.price, 2)
    }
    val availableQty = remember(product.actualQty) {
        formatDoubleToString(product.actualQty, 0)
    }
    var baseUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        baseUrl = "https://erp-ni.distribuidorareyes.com" // actions.fetchBaseUrl()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val imageUrl = baseUrl + product.image

            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp),
                contentDescription = "Imagen de ${product.name}"
            )

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    product.name, // Manejar nulos
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Disp: $availableQty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.actualQty > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                Text(
                    "Cód: ${product.itemCode}", // Manejar nulos
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "C$$formattedPrice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- Componentes de Estado de Pantalla (Loading, Error, Empty) ---
@Composable
private fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            trackColor = Color.Blue,
            color = Color.Cyan,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun FullScreenErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CloudOff,
                "Error",
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Función de extensión para el modificador de brillo (shimmer)
fun Modifier.shimmerBackground(shape: CornerBasedShape = RoundedCornerShape(4.dp)): Modifier =
    composed {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f, // Ajusta este valor según el tamaño de tu item y la velocidad deseada
            animationSpec = infiniteRepeatable(
                tween(durationMillis = 1200, easing = FastOutSlowInEasing), // Duración del brillo
                RepeatMode.Restart
            ),
            label = "shimmerTranslateAnim"
        )

        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), // Color base del placeholder
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Color más claro para el brillo
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnim - 500f, translateAnim - 500f),
                end = Offset(translateAnim, translateAnim)
            ),
            shape = shape
        )
    }

@Composable
fun PlaceholderProductRowItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large, // Usa la misma forma que tu ProductRowItem
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Fondo de la tarjeta
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Mismo padding que ProductRowItem
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                // Placeholder para el nombre del producto (dos líneas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // Ancho del placeholder del título
                        .height(20.dp) // Altura de una línea de título
                        .shimmerBackground(MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f) // Segunda línea un poco más corta
                        .height(20.dp)
                        .shimmerBackground(MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(8.dp)) // Más espacio antes de los detalles

                // Placeholder para "Disp:"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp) // Altura de una línea de cuerpo de texto
                        .shimmerBackground(MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Placeholder para "Cód:"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp) // Altura de una línea de cuerpo de texto pequeño
                        .shimmerBackground(MaterialTheme.shapes.small)
                )
            }
            // Placeholder para el precio
            Box(
                modifier = Modifier
                    .width(60.dp) // Ancho aproximado del precio
                    .height(24.dp) // Altura del precio
                    .shimmerBackground(MaterialTheme.shapes.small)
            )
        }
    }
}

@Preview
@Composable
fun ProductRowItemPreview() {
    ProductRowItem(
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
        )
    )
}

@Preview
@Composable
fun PlaceholderProductRowItemPreview() {
    MaterialTheme { // Asegúrate de envolverlo en tu tema para que los colores funcionen
        PlaceholderProductRowItem()
    }
}

@Preview
@Composable
fun PlaceholderListPreview() {
    MaterialTheme {
        Column {
            repeat(3) {
                PlaceholderProductRowItem(modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

@Preview()
@Composable
fun InventoryScreenSuccessPreview() {
    val items = listOf(
        ItemBO(
            "Palitos de pollo", "Palitos de pollo congelados, cada de 400gr",
            "AVPLCJ400", "1234567890", "", "CARNES", "Frito Lay",
            100.456, 10.0, 0.0, false, false, "UND"
        ),
        ItemBO(
            "Palitos de pollo", "Palitos de pollo congelados, cada de 400gr",
            "AVPLCJ400", "1234567890", "", "CARNES", "Frito Lay",
            100.456, 10.0, 0.0, false, false, "UND"
        ),
        ItemBO(
            "Palitos de pollo", "Palitos de pollo congelados, cada de 400gr",
            "AVPLCJ400", "1234567890", "", "CARNES", "Frito Lay",
            100.456, 10.0, 0.0, false, false, "UND"
        ),
        ItemBO(
            "Palitos de pollo", "Palitos de pollo congelados, cada de 400gr",
            "AVPLCJ400", "1234567890", "", "CARNES", "Frito Lay",
            100.456, 10.0, 0.0, false, false, "UND"
        ),
        ItemBO(
            "Palitos de pollo", "Palitos de pollo congelados, cada de 400gr",
            "AVPLCJ400", "1234567890", "", "CARNES", "Frito Lay",
            100.456, 10.0, 0.0, false, false, "UND"
        ),
    )
    val flow: Flow<PagingData<ItemBO>> = flowOf(PagingData.from(items))
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Success(flow, listOf(CategoryBO("CARNES"))),
            actions = InventoryAction()
        )
    }
}

@Composable
@Preview
fun InventoryScreenSuccessNoResultsPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Success(flowOf(PagingData.from(listOf())), listOf()),
            actions = InventoryAction()
        )
    }
}

@Preview()
@Composable
fun InventoryScreenLoadingPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Loading,
            actions = InventoryAction()
        )
    }
}

@Preview()
@Composable
fun InventoryScreenErrorPreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Error("Esto es un error de prueba"),
            actions = InventoryAction()
        )
    }
}

@Preview()
@Composable
fun InventoryScreenEmptyStatePreview() {
    MaterialTheme {
        InventoryScreen(
            state = InventoryState.Empty,
            actions = InventoryAction()
        )
    }
}
