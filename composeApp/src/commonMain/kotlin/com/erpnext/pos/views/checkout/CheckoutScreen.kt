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
import com.erpnext.pos.views.checkout.CheckoutAction
import com.erpnext.pos.views.checkout.CheckoutState
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    state: CheckoutState,
    action: CheckoutAction
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva Venta") }) }
    ) { padding ->

        when (state) {
            is CheckoutState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is CheckoutState.Success -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text("Cliente", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = { /* TODO: Navegar */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seleccionar Cliente")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, null)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Encabezado
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Artículo", Modifier.weight(2f))
                        Text("Cant.", Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Tarifa", Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("SubTotal", Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    Divider()

                    state.products.forEach { item ->
                        val subtotal = (item.price ?: 0.0) * (item.actualQty ?: 0.0)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(2f)) {
                                Text(item.name ?: "-", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Código: ${item.itemCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            OutlinedTextField(
                                value = (item.actualQty ?: 0.0).toString(),
                                onValueChange = { qty ->
                                    /*action.updateQuantity(
                                        item.itemCode ?: "",
                                        qty.toDoubleOrNull() ?: 0.0
                                    )*/
                                },
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Text(
                                "C$ ${item.price ?: 0.0}",
                                Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                "C$ ${subtotal}",
                                Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        Divider()
                    }

                    Spacer(Modifier.height(16.dp))
                    SummaryRow("Subtotal", 100.0) //action.subtotal)
                    SummaryRow("Impuestos", 15.0) //action.tax)
                    SummaryRow("Descuento", 0.0) //action.discount)
                    Divider()
                    SummaryRow("Total", 115.0, bold = false) //total, bold = true)

                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { /* TODO: Guardar factura en ERPNext */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Finalizar venta", color = Color.White)
                    }
                }
            }

            is CheckoutState.Empty -> null
            is CheckoutState.Error -> null
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
        Text(
            label,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            "C$ $amount",
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
@Preview()
private fun CheckoutScreenPreview() {
    CheckoutScreen(
        state = CheckoutState.Success(emptyList(), emptyList()),
        action = CheckoutAction()
    )
}