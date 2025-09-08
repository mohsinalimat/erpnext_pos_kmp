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
                    // Contenedor para los actions, si necesitas espaciado específico entre ellos
                    // Row(verticalAlignment = Alignment.CenterVertically) {

                    // Acción 1: Settings
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Spacer(Modifier.width(4.dp)) // Espacio opcional entre iconos

                    // Acción 2: Refresh
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Spacer(Modifier.width(4.dp)) // Espacio opcional entre iconos

                    // Acción 3: Online Prediction
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OnlinePrediction,
                            contentDescription = "Online Prediction",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // } // Fin del Row opcional
                }
                // colors = TopAppBarDefaults.smallTopAppBarColors(
                //    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                // )
            )
        })
    { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = 12.dp, bottom = 0.dp, end = 16.dp, start = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de Bienvenida y banners
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp)) // Reducimos un poco el spacer
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
