package com.erpnext.pos.views

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNuevaVenta: () -> Unit = {},
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(), // El padding se maneja mejor internamente o en el contenido
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(), // El padding interno de TopAppBar es suficiente
                title = {
                    Text(
                        text = "ERPNext POS",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy( // Título más grande
                            fontWeight = FontWeight.Bold, // Más énfasis en el título
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.OnlinePrediction, contentDescription = "Online Prediction"
                        )
                    }
                }
            )
        })
    { paddingValues ->
        // Aplicamos el padding del Scaffold al contenido principal
        Column(
            modifier = Modifier
                .padding(paddingValues) // Padding del Scaffold aplicado aquí
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp), // Padding general para el contenido interno
            horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido horizontalmente
        ) {
            // Sección de Bienvenida y banners
            Column( // Usamos una Column para que el Text de banner esté debajo del de bienvenida
                modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp)) // Reducimos un poco el spacer
                Text(
                    "Espacio para agregar banners de notificacion -> General en v1",
                    style = MaterialTheme.typography.bodyMedium // Estilo más apropiado
                )
            }

            Spacer(Modifier.height(24.dp))

            // Tarjetas de resumen
            Column( // Usamos Column para apilar las tarjetas verticalmente
                modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp) // Padding aumentado para mejor espaciado interno
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically // Centra el icono verticalmente
                    ) {
                        Column(modifier = Modifier.weight(1f)) { // Permite que el texto ocupe el espacio disponible
                            Text("Stock pendiente por recibir", fontWeight = FontWeight.Bold)
                            Text("Tienes 3 productos en espera")
                        }
                        Spacer(Modifier.width(8.dp)) // Espacio entre texto e icono
                        Icon(Icons.Default.Warehouse, contentDescription = null)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp) // Padding aumentado
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recarga pendiente por recibir", fontWeight = FontWeight.Bold)
                            Text("Tienes 2 recargas en espera")
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                    }
                }
            }

            // Espacio flexible para empujar el botón hacia abajo
            Spacer(Modifier.weight(1f))

            // Botón principal
            // No es necesario un Row y Column adicional si solo hay un botón
            Button(
                onClick = onNuevaVenta,
                modifier = Modifier
                    .fillMaxWidth() // El botón ocupa el ancho
                    .padding(bottom = 16.dp), // Padding en la parte inferior para separarlo del borde
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // Considera usar MaterialTheme.colorScheme.primary
            ) {
                Text("Abrir Caja")
            }
        }
    }
}

@Composable
@Preview
fun HomePreview() {
    MaterialTheme { // Es buena práctica envolver los Previews en MaterialTheme
        HomeScreen({})
    }
}
