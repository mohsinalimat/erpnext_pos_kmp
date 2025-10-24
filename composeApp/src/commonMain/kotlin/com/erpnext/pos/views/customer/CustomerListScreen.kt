package com.erpnext.pos.views.customer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.views.home.toCurrencySymbol
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    oState: CustomerState,
    actions: CustomerAction
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedTerritory by remember { mutableStateOf("Todas") }

    val customers = (oState as? CustomerState.Success)?.customers ?: emptyList()
    val territories = (oState as? CustomerState.Success)?.territories ?: emptyList()

    val filteredCustomers = customers.filter {
        it.customerName.contains(searchQuery, ignoreCase = true) ||
                (it.mobileNo ?: "").contains(searchQuery, ignoreCase = true)
    }

    val isContentScrolledUnderFilters by remember {
        derivedStateOf {
            // Para customers, usa LazyColumn state si agregas
            false // Placeholder; ajusta si tienes LazyListState
        }
    }

    val filterElevation by animateDpAsState(
        targetValue = if (isContentScrolledUnderFilters) 4.dp else 0.dp,
        label = "filterElevation"
    )

    LaunchedEffect(Unit) {
        actions.fetchAll()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Clientes", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = actions.onRefresh) {
                        Icon(Icons.Filled.Refresh, "Actualizar Clientes")
                    }
                    IconButton(onClick = { /* Toggle online */ }) {
                        Icon(Icons.Filled.OnlinePrediction, "Modo Online")
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val currentState = oState) {
                CustomerState.Loading -> {
                    FullScreenLoadingIndicator()
                }

                is CustomerState.Error -> {
                    FullScreenErrorMessage(
                        errorMessage = currentState.message,
                        onRetry = actions.fetchAll
                    )
                }

                CustomerState.Empty -> {
                    EmptyStateMessage(
                        message = "No hay clientes disponibles.",
                        icon = Icons.Filled.People
                    )
                }

                is CustomerState.Success -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = filterElevation,
                        shadowElevation = filterElevation,
                    ) {
                        CustomerFilters(
                            searchQuery = searchQuery,
                            selectedTerritory = selectedTerritory,
                            territories = territories,
                            onQueryChange = { query ->
                                searchQuery = query
                                actions.onSearchQueryChanged(query)
                            },
                            onTerritoryChange = { territory ->
                                actions.onTerritorySelected(territory)
                                selectedTerritory = territory ?: "Todos"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                        )
                    }

                    if (filteredCustomers.isEmpty() && searchQuery.isNotEmpty()) {
                        EmptyStateMessage(
                            message = "No se encontraron clientes que coincidan con tu búsqueda.",
                            icon = Icons.Filled.SearchOff
                        )
                    } else if (filteredCustomers.isEmpty()) {
                        EmptyStateMessage(
                            message = "No hay clientes en esta selección.",
                            icon = Icons.Filled.People
                        )
                    } else {
                        CustomerListContent(
                            filteredCustomers = filteredCustomers,
                            actions = actions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerFilters(
    searchQuery: String,
    selectedTerritory: String,
    territories: List<String?>,
    onQueryChange: (String) -> Unit,
    onTerritoryChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (!territories.isEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = selectedTerritory == "Todas"
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTerritoryChange("Todas") },
                        label = { Text("Todas") },
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
                items(territories) { territory ->
                    val isSelected = territory == selectedTerritory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTerritoryChange(territory) },
                        label = { Text(territory ?: "Todos") },
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
            placeholderText = "Buscar cliente por nombre o teléfono..."
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
        onValueChange = { query -> onSearchQueryChange(query) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { Text(placeholderText, style = MaterialTheme.typography.bodyLarge, overflow = TextOverflow.Ellipsis, softWrap = true, maxLines = 1) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Icono de búsqueda",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = {
                    onSearchQueryChange("")
                    keyboardController?.show()
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun CustomerListContent(
    filteredCustomers: List<CustomerBO>,
    actions: CustomerAction
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filteredCustomers) { customer ->
            CustomerItem(customer = customer) {
                actions.toDetails(customer.name)
            }
        }
    }
}

@Composable
fun CustomerItem(customer: CustomerBO, onClick: () -> Unit) {
    val isOverLimit = customer.availableCredit < 0 || customer.currentBalance > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circular
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOverLimit) MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.2f
                    ) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = customer.customerName,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                    tint = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido expandible
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    customer.customerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    customer.mobileNo ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOverLimit) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    customer.territory ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Columna derecha para balance/pendientes
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${customer.currency.toCurrencySymbol()}${customer.currentBalance}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isOverLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    "${customer.pendingInvoices} pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverLimit) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Componentes de Estado de Pantalla ---
@Composable
private fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                errorMessage,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                message,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun Modifier.shimmerBackground(shape: RoundedCornerShape = RoundedCornerShape(4.dp)): Modifier =
    composed {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                RepeatMode.Restart
            ),
            label = "shimmerTranslateAnim"
        )

        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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

@Preview
@Composable
fun CustomerListScreenPreview() {
    MaterialTheme {
        CustomerListScreen(
            oState = CustomerState.Success(
                customers = listOf(
                    CustomerBO(
                        name = "1",
                        customerName = "Ricardo García",
                        territory = "Managua",
                        mobileNo = "+505 8888 0505",
                        customerType = "Individual",
                        currentBalance = 13450.0,
                        pendingInvoices = 2,
                        availableCredit = 0.0
                    )
                ),
                territories = listOf("Managua", "León")
            ),
            actions = CustomerAction()
        )
    }
}

@Preview
@Composable
fun CustomerListScreenLoadingPreview() {
    MaterialTheme {
        CustomerListScreen(
            oState = CustomerState.Loading,
            actions = CustomerAction()
        )
    }
}

@Preview
@Composable
fun CustomerListScreenErrorPreview() {
    MaterialTheme {
        CustomerListScreen(
            oState = CustomerState.Error("Error al cargar clientes"),
            actions = CustomerAction()
        )
    }
}

@Preview
@Composable
fun CustomerListScreenEmptyPreview() {
    MaterialTheme {
        CustomerListScreen(
            oState = CustomerState.Empty,
            actions = CustomerAction()
        )
    }
}

@Preview
@Composable
fun CustomerItemPreview() {
    MaterialTheme {
        CustomerItem(
            customer = CustomerBO(
                name = "1",
                customerName = "Ricardo García",
                territory = "Managua",
                mobileNo = "+505 8888 0505",
                customerType = "Individual",
                currentBalance = 13450.0,
                pendingInvoices = 2,
                availableCredit = 0.0
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun CustomerItemOverLimitPreview() {
    MaterialTheme {
        CustomerItem(
            customer = CustomerBO(
                name = "2",
                customerName = "Sofía Ramírez",
                territory = "León",
                mobileNo = "+505 7777 0404",
                customerType = "Company",
                currentBalance = 0.0,
                pendingInvoices = 0,
                availableCredit = -500.0  // Sobre límite para rojo
            ),
            onClick = {}
        )
    }
}