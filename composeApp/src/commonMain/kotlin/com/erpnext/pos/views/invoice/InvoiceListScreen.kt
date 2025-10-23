package com.erpnext.pos.views.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("Facturación") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Filtros
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedDate ?: "Seleccionar fecha")
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = {
                        clientName = it
                        action.onCustomerSelected(it)
                    },
                    placeholder = { Text("Nombre de Cliente") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            when (state) {
                is InvoiceState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is InvoiceState.Error -> {
                    Text(
                        state.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is InvoiceState.Empty -> {
                    Text("No hay facturas", style = MaterialTheme.typography.bodyMedium)
                }

                is InvoiceState.Success -> {
                    val invoices = state.invoices.collectAsLazyPagingItems()

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(invoices.itemCount) { index ->
                            val invoice = invoices[index] ?: return@items

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        invoice.invoiceId,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Cliente: ${invoice.customer}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Estado: ${invoice.status}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Fecha: ${invoice.postingDate}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    "C$ ${invoice.total}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Divider()
                        }
                    }
                }
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = true
                                //action.updateDateFilter(datePickerState.selectableDates.)
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
    }
}

