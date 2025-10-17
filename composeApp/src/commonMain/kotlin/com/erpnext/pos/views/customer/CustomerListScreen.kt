package com.erpnext.pos.views.customer

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erpnext.pos.domain.models.CustomerBO
import com.erpnext.pos.views.customer.CustomerState.Success
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    oState: CustomerState,
    actions: CustomerAction
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTerritory by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Usa territories de oState.Success
    val territories = (oState as? Success)?.territories ?: emptyList()

    // Filtrado local reactivo (de oState.success.customers)
    val customers = (oState as? Success)?.customers ?: emptyList()
    val filteredCustomers = customers.filter { customer ->
        customer.customerName.contains(searchQuery, ignoreCase = true) ||
                (customer.mobileNo ?: "").contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                actions = {
                    IconButton(onClick = { actions.onRefresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = { /*actions.onToggleOnline()*/ }) {
                        Icon(Icons.Filled.OnlinePrediction, contentDescription = "Modo Online")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (territories.isNotEmpty()) {
                // Dropdown para Territorio
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedTerritory ?: "Todas",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Territorio") },
                            leadingIcon = {
                                Icon(Icons.Filled.LocationOn, contentDescription = "Territorio")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    actions.onTerritorySelected(null)
                                    selectedTerritory = null
                                    expanded = false
                                }
                            )
                            territories.forEach { territory ->
                                DropdownMenuItem(
                                    text = { Text(territory ?: "N/A") },
                                    onClick = {
                                        actions.onTerritorySelected(territory)
                                        selectedTerritory = territory
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Buscador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        actions.onSearchQueryChanged(it)
                    },
                    label = { Text("Buscar cliente") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                actions.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    singleLine = true
                )
            }

            // Lista de clientes con Crossfade para estados
            Crossfade(
                targetState = oState,
                modifier = Modifier.fillMaxSize(),
                /*animationSpec = {
                    fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }*/
            ) { state ->
                when (state) {
                    is CustomerState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    is CustomerState.Success -> {
                        if (filteredCustomers.isEmpty()) {
                            EmptyState(
                                icon = Icons.Filled.SearchOff,
                                title = "No hay resultados",
                                subtitle = "Prueba con otro término de búsqueda",
                                buttonText = "Limpiar",
                                onButtonClick = {
                                    searchQuery = ""
                                    actions.onSearchQueryChanged("")
                                }
                            )
                        } else {
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
                    }

                    is CustomerState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { actions.fetchAll() }
                        )
                    }

                    is CustomerState.Empty -> {
                        EmptyState(
                            icon = Icons.Filled.People,
                            title = "No hay clientes",
                            subtitle = "Sincroniza para cargar datos",
                            buttonText = "Sincronizar",
                            onButtonClick = { actions.onRefresh() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItem(customer: CustomerBO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = customer.customerName,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    customer.mobileNo ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    "C\$${customer.currentBalance}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (customer.currentBalance > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    "${customer.pendingInvoices} pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                message,
                style = MaterialTheme.typography.headlineSmall,
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

@Preview
@Composable
fun CustomerListScreenPreview() {
    val mockState = CustomerState.Success(
        customers = listOf(
            CustomerBO(
                name = "1",
                customerName = "Ricardo García",
                territory = "Managua",
                mobileNo = "+505 8888 0505",
                customerType = "Individual",
                currentBalance = 13450.0,
                pendingInvoices = 2
            )
        )
    )
    CustomerListScreen(oState = mockState, actions = CustomerAction())
}

@Preview()
@Composable
fun CustomerItemPreview() {
    CustomerItem(
        customer = CustomerBO(
            name = "1",
            customerName = "Ricardo García",
            territory = "Managua",
            mobileNo = "+505 8888 0505",
            currentBalance = 13450.0,
            pendingInvoices = 2,
            address = "Managua",
            creditLimit = listOf(),
            customerType = "Regular",
            availableCredit = 13000.0,
            totalPendingAmount = 4000.0
        ),
        onClick = {}
    )
}

@Preview()
@Composable
fun EmptyStatePreview() {
    EmptyState(
        icon = Icons.Filled.SearchOff,
        title = "No hay resultados",
        subtitle = "Prueba con otro término de búsqueda",
        buttonText = "Limpiar",
        onButtonClick = {}
    )
}

@Preview()
@Composable
fun ErrorStatePreview() {
    ErrorState(
        message = "Error al cargar clientes",
        onRetry = {}
    )
}