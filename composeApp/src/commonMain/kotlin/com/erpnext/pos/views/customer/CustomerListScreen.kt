package com.erpnext.pos.views.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val balance: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onNavigate: () -> Unit,
    onCustomerClick: (Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val customers = listOf(
        Customer("1", "Ricardo García", "+505 8888 0505", "C\$13,450"),
        Customer("2", "Sofía Ramírez", "+505 7777 0404", "C\$0"),
        Customer("3", "Carlos Mendoza", "+505 6666 0303", "C\$2,500"),
        Customer("4", "Ana López", "+505 5555 0202", "C\$1,200"),
        Customer("5", "Martha Palacios", "+505 1236 4968", "C\$0"),
        Customer("6", "Elizabeth Gomez", "+505 7702 0269", "C\$0"),
        Customer("7", "Heriberto Mendoza", "+505 8375 9875", "C\$0")
    )

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Update")
                    }
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
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar cliente") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Lista de clientes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCustomers) { customer ->
                    CustomerItem(customer = customer) {
                        onCustomerClick(customer)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(customer.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
            }
            Text(customer.balance, fontWeight = FontWeight.Medium)
        }
    }
}
