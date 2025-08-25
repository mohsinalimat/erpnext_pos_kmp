import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigate: () -> Unit,
//    onSelectCustomer: () -> Unit,
//    onAddProduct: () -> Unit,
//    onFinishSale: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(0.00) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Cliente
            Text("Cliente", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = {
                    //nNavigate("customer_list/for_checkout")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar Cliente")
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val items = listOf(
                Triple("Kings - Papas Fritas 3/8 Golden", "KING0023", 20.00),
                Triple("AVK - Papa 3/8 C. Recto grado B", "AVIK0003", 10.00),
                Triple("Kb - Medio Pollo", "KIMB0007", 90.00)
            )

            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Artículo", modifier = Modifier.weight(2f))
                Text("Cantidad", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Tarifa", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("SubTotal", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            Divider()

            items.forEach { (name, sku, price) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Código: $sku",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    OutlinedTextField(
                        value = searchQuery.toString(),
                        onValueChange = { newValue -> {

                        } },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text(
                        "C$ $price",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "C$ 0.00",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Divider()
            }

            // Botón agregar artículo
            OutlinedButton(
                onClick = { },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Artículo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen
            Text("Resumen", style = MaterialTheme.typography.titleMedium)
            SummaryRow("Subtotal", 120.00)
            SummaryRow("Impuestos", 12.00)
            SummaryRow("Descuento", 0.00)
            Divider()
            SummaryRow("Total", 132.00, bold = true)

            Spacer(modifier = Modifier.weight(1f))

            // Botón Finalizar Venta
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Finalizar venta", color = Color.White)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(
            "C$ $amount",
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
@Preview()
private fun CheckoutScreenPreview() {
    CheckoutScreen {}
}