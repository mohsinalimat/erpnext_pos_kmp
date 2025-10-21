package com.erpnext.pos.views.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.models.PaymentModesBO
import com.erpnext.pos.remoteSource.dto.BalanceDetailsDto
import com.erpnext.pos.remoteSource.dto.POSOpeningEntryDto
import com.erpnext.pos.views.CashBoxManager
import io.ktor.util.date.getTimeMillis
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeState,
    actions: HomeAction,
) {
    var showDialog by remember { mutableStateOf(false) }
    var currentProfiles by remember { mutableStateOf(emptyList<POSProfileBO>()) }
    var isOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        //actions.loadPOSProfile()
        //actions.loadUserInfo()
        isOpen = actions.isCashboxOpen()
    }

    LaunchedEffect(uiState) {
        if (uiState is HomeState.POSProfiles && currentProfiles.isEmpty()) {
            currentProfiles = uiState.posProfiles
        }

        if (uiState is HomeState.CashboxState) {
            isOpen = uiState.isOpen
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(modifier = Modifier.fillMaxWidth(), title = {
                Text(
                    text = "ERPNext POS",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                    )
                )
            }, actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.Filled.OnlinePrediction, contentDescription = "Online Prediction"
                    )
                }
            })
        }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 12.dp, start = 12.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 💡 Contenido principal
            when (uiState) {
                is HomeState.Loading -> FullScreenLoadingIndicator()
                is HomeState.LoadingProfiles -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    trackColor = Color.Blue,
                    color = Color.Cyan,
                    strokeWidth = 2.dp
                )

                is HomeState.Success, is HomeState.POSProfiles, is HomeState.POSInfoLoaded, is HomeState.POSInfoLoading, is HomeState.CashboxState -> {
                    // Saludo y banners
                    Column(
                        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Bienvenido",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Espacio para banners de notificación -> General en v1",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Tarjetas resumen
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Stock pendiente por recibir", fontWeight = FontWeight.Bold
                                    )
                                    Text("Tienes 3 productos en espera")
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Warehouse, contentDescription = null)
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Recarga pendiente por recibir",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Tienes 2 recargas en espera")
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.CreditCard, contentDescription = null)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Botón abrir caja
                    Button(
                        onClick = {
                            if (isOpen) {
                                actions.closeCashbox()
                            } else {
                                if (currentProfiles.isNotEmpty()) {
                                    showDialog = true // 🔥 Solo abre el dialog, no recarga
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isOpen) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = if (isOpen) "Cerrar Caja" else "Abrir Caja",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                }

                is HomeState.Error -> FullScreenErrorMessage(uiState.message, {})
            }

            if (showDialog && currentProfiles.isNotEmpty()) {
                POSProfileDialog(uiState = uiState, profiles = currentProfiles, onSelectProfile = {
                    actions.onPosSelected(it)
                }, onOpenCashbox = { pos, amounts ->
                    val openEntry = POSOpeningEntryDto(
                        pos.name,
                        pos.company,
                        getTimeMillis(),
                        user = null,
                        status = true,
                        amounts.map { BalanceDetailsDto(it.mode.name, it.amount) })
                    actions.openCashbox(openEntry)
                }, onDismiss = {
                    actions.initialState()
                    showDialog = false
                })
            }
        }
    }
}

@Composable
fun POSProfileDialog(
    uiState: HomeState,
    profiles: List<POSProfileBO>,
    onSelectProfile: (POSProfileBO) -> Unit,
    onOpenCashbox: (POSProfileBO, List<PaymentModeWithAmount>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProfile by remember { mutableStateOf<POSProfileBO?>(null) }
    var paymentAmounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            when (uiState) {
                is HomeState.POSInfoLoading -> "Cargando configuración..."
                is HomeState.POSInfoLoaded -> "Balance de Apertura"
                else -> "Seleccione un POS:"
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }, text = {
        // Paso 1 → mostrar perfiles
        Column {
            LazyColumn {
                items(profiles) { profile ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                            selectedProfile = profile
                            onSelectProfile(profile) // 🔥 VM carga info
                        },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                profile.company, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            when (uiState) {
                // Loading mientras el VM trae la info
                is HomeState.POSInfoLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            trackColor = Color.Blue,
                            color = Color.Cyan,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Paso 2 → mostrar payment modes
                is HomeState.POSInfoLoaded -> {
                    val modes = uiState.info.paymentModes
                    LazyColumn(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(modes) { mode ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(0.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        mode.modeOfPayment,
                                        modifier = Modifier.width(125.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                        )
                                    )
                                    NumericCurrencyTextField(
                                        value = paymentAmounts[mode.name] ?: "",
                                        onValueChange = {
                                            paymentAmounts = paymentAmounts.toMutableMap().apply {
                                                put(mode.name, it)
                                            }
                                        },
                                        placeholder = "0.0",
                                        modifier = Modifier.width(100.dp),
                                        currencySymbol = uiState.currency.toCurrencySymbol()
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }, confirmButton = {
        when (uiState) {
            is HomeState.POSProfiles -> {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }

            is HomeState.POSInfoLoaded -> {
                Row {
                    TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amounts = uiState.info.paymentModes.map {
                                PaymentModeWithAmount(
                                    mode = it,
                                    amount = paymentAmounts[it.name]?.toDoubleOrNull() ?: 0.0
                                )
                            }
                            selectedProfile?.let { profile ->
                                onOpenCashbox(profile, amounts)
                                onDismiss()
                            }
                        }) {
                        Text("Abrir Caja")
                    }
                }
            }

            else -> {}
        }
    })
}

@Composable
fun NumericCurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "C$",
    placeholder: String = "0.00"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val finalValue = if (filtered.count { it == '.' } > 1) {
                filtered.dropLast(1)
            } else filtered
            onValueChange(finalValue)
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Text(
                text = currencySymbol, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Black
                ), color = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number, // Numérico con decimales
            imeAction = ImeAction.Done
        ),
        textStyle = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Black,
        )
    )
}

@Composable
private fun FullScreenErrorMessage(
    errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CloudOff,
                "Error",
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun FullScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            trackColor = Color.Blue,
            color = Color.Cyan,
            strokeWidth = 2.dp
        )
    }
}

@Composable
@Preview
fun HomePreview() {
    MaterialTheme {
        HomeScreen(HomeState.Loading, HomeAction())
    }
}

@Composable
@Preview
fun NumericCurrencyTextFieldPreview() {
    NumericCurrencyTextField("658.93", {}, currencySymbol = "C$")
}

// ---- Models auxiliares ----
data class PaymentModeWithAmount(
    val mode: PaymentModesBO, val amount: Double
)

fun String.toCurrencySymbol(): String {
    return when (this) {
        "NIO" -> "C$"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> ""

    }
}