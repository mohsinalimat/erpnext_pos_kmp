package com.erpnext.pos.views.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.erpnext.pos.domain.models.PendingInvoiceBO
import com.erpnext.pos.views.inventory.components.SearchTextField
import kotlinx.datetime.*
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun InvoiceListScreen(
    state: InvoiceState,
    action: InvoiceAction
) {
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var clientName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Facturación", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                /** 🔍 Barra de Filtros **/
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchTextField(clientName, {
                            clientName = it
                            action.onCustomerSelected(it)
                        })
                        /*OutlinedTextField(
                            value = clientName,
                            onValueChange = {
                                clientName = it
                                action.onCustomerSelected(it)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Buscar cliente") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            shape = MaterialTheme.shapes.large
                        )*/

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(selectedDate ?: "Fecha")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /** 💼 Contenido **/
                when (state) {
                    is InvoiceState.Loading -> LoadingState()
                    is InvoiceState.Error -> ErrorState(state.error)
                    is InvoiceState.Empty -> EmptyState()
                    is InvoiceState.Success -> {
                        val invoices = state.invoices.collectAsLazyPagingItems()

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(invoices.itemCount) { index ->
                                val invoice = invoices[index] ?: return@items
                                InvoiceCard(invoice = invoice)
                            }
                        }
                    }
                }

                /** 🗓️ Date Picker **/
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val millis = datePickerState.selectedDateMillis
                                    millis?.let {
                                        val instant = Instant.fromEpochMilliseconds(it)
                                        val localDateTime =
                                            instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                        val formatted = buildString {
                                            append(localDateTime.year.toString().padStart(2, '0'))
                                            append("/")
                                            append(localDateTime.month.toString().padStart(2, '0'))
                                            append("/")
                                            append(localDateTime.day)
                                        }
                                        selectedDate = formatted
                                        action.onDateSelected(formatted)
                                    }
                                    showDatePicker = false
                                }
                            ) { Text("Aceptar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            /** 🔄 Overlay de Carga **/
            if (state is InvoiceState.Loading) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }
    }
}

/** 💳 Tarjeta estilizada para cada factura **/
@Composable
fun InvoiceCard(invoice: PendingInvoiceBO) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, MaterialTheme.shapes.medium),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Factura #${invoice.invoiceId}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Cliente: ${invoice.customer}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Estado: ${invoice.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fecha: ${invoice.postingDate}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "C$ ${invoice.total}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 🌐 Estados de interfaz elegantes **/

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No hay facturas disponibles", style = MaterialTheme.typography.bodyMedium)
    }
}
