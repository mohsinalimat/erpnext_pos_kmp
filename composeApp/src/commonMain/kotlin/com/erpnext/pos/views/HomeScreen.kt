package com.erpnext.pos.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeScreen(
    onNavigate: () -> Unit,
    onNuevaVenta: () -> Unit = {},
) {

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("ERPNext POS") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Update")
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.OnlinePrediction,
                            contentDescription = "Online Prediction"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
                .padding(end = 12.dp, start = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column {
                Text(
                    "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(15.dp))
                Text("Espacio para agregar banners de notificacion -> General en v1")
            }

            Spacer(Modifier.height(24.dp))

            // Tarjetas de resumen
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stock pendiente por recibir", fontWeight = FontWeight.Bold)
                            Text("Tienes 3 productos en espera")
                        }
                        Icon(Icons.Default.Warehouse, contentDescription = null)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Recarga pendiente por recibir", fontWeight = FontWeight.Bold)
                            Text("Tienes 2 recargas en espera")
                        }
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botones principales
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onNuevaVenta,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Abrir Caja")
                }

            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
@Preview
fun HomePreview() {
    HomeScreen({}, {})
}
