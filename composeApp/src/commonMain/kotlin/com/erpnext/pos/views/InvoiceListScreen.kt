package com.erpnext.pos.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

data class Sale(
    val id: String,
    val status: String,
    val client: String,
    val total: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun InvoiceListScreen(
    onNavigate: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("Seleccionar fecha") }
    var clientName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val sales = listOf(
        Sale("RTI #2025-005", "Pagado", "Sofia Ramirez", "C\$150"),
        Sale("RCS #2025-004", "Vencida", "Carlos Mendoza", "C\$200"),
        Sale("RTP #2025-003", "Cancelada", "Ana Lopez", "C\$100"),
        Sale("CI #2025-002", "Borrador", "Diego Vargas", "C\$250")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Facturacion") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.OnlinePrediction, contentDescription = "Online Prediction")
                    }
                }
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
            Text("Filtros", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedDate)
                }
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    placeholder = { Text("Nombre de Cliente") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Lista de ventas
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sales) { sale ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(sale.id, style = MaterialTheme.typography.bodyLarge)
                            Text("Estado: ${sale.status}", style = MaterialTheme.typography.bodySmall)
                            Text("Cliente: ${sale.client}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(sale.total, style = MaterialTheme.typography.bodyLarge)
                    }
                    Divider()
                }
            }


            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

        }
    }
}
