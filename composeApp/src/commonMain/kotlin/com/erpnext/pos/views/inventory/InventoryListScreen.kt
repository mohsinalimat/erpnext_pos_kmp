package com.erpnext.pos.views.inventory

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction

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
    state: InventoryState, actions: InventoryAction
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
                title = {
                    Text(
                        text = "Inventario",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.OnlinePrediction,
                            contentDescription = "Online Prediction",
                            modifier = Modifier.size(16.dp)
                        )
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
        onValueChange = onSearchQueryChange,
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
