package com.erpnext.pos.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

data class InventoryItem(
    val name: String,
    val availableQty: Double,
    val code: String,
    val price: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun InventoryScreen(
    onNavigate: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Pollo", "Res", "Cerdo", "Papas", "Embutidos")

    // Datos de prueba
    val products = listOf(
        InventoryItem("Kings - Papas Fritas 3/8 Golden", 100101.00, "KING0023", 44.09),
        InventoryItem("Kings - Papas Golden Grado C", 22.00, "KING0030", 5.49),
        InventoryItem("Kings - Papa 1/4 Golden Phoenix", 25.00, "KING0021", 68.99),
        InventoryItem("AVK - Papa 3/8 C. Recto grado B", 75.00, "AVIK0003", 7.99),
        InventoryItem("TT - Pechuga C/A", 120.00, "TIP0026", 12.49),
        InventoryItem("Kb - Medio Pollo", 40.00, "KIMB0007", 14.57),
        InventoryItem("Kb - Pierna Entera", 43.25, "KIMB0011", 45.14),
        InventoryItem("TT - Pechuga Cono", 48.69, "TIP0056", 29.65)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Update")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.OnlinePrediction, contentDescription = "Online Prediction")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Acción imprimir */ }) {
                Icon(Icons.Default.Print, contentDescription = "Imprimir")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Categorías
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de productos
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.code.contains(searchQuery, ignoreCase = true)
                }) { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text(
                                "Disponible: ${product.availableQty}",
                                fontSize = 12.sp
                            )
                            Text(
                                "Código: ${product.code}",
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            "C$${product.price}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
