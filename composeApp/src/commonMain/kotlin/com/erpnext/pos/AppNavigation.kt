package com.erpnext.pos

import CheckoutScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erpnext.pos.screens.*
import androidx.compose.runtime.getValue
import com.erpnext.pos.navigation.BottomBarNavigation
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.screens.customer.CustomerDetailScreen
import com.erpnext.pos.screens.customer.CustomerListScreen

fun shouldShowBottomBar(currentRoute: String): Boolean {
    return currentRoute !in listOf(NavRoute.Login.path, NavRoute.AuthCheck.path)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val visibleEntries by navController.visibleEntries.collectAsState()
    val currentRoute = visibleEntries.lastOrNull()?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomBarNavigation(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(navController, startDestination = NavRoute.Login.path) {
                composable(NavRoute.Login.path) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(NavRoute.Home.path) {
                                popUpTo(NavRoute.Login.path) { inclusive = true }
                            }
                        }
                    )
                }
                composable(NavRoute.Home.path) {
                    HomeScreen(onNavigate = { navController.navigate(NavRoute.Inventory.path) })
                }
                composable(NavRoute.Inventory.path) {
                    InventoryScreen(onNavigate = { navController.navigate(NavRoute.Sale.path) })
                }
                composable(NavRoute.Sale.path) {
                    CheckoutScreen(onNavigate = { navController.navigate(NavRoute.Customer.path) })
                }
                composable(NavRoute.Customer.path) {
                    CustomerListScreen(
                        onNavigate = { navController.navigate(NavRoute.Customer.path) },
                        onCustomerClick = { customer ->
                            navController.navigate("${NavRoute.CustomerDetail.path}/${customer.id}")
                        }
                    )
                }
                composable("${NavRoute.CustomerDetail.path}/{customerId}") { backStackEntry ->
                    //val customerId = backStackEntry.path<String>("customerId") ?: ""
                    CustomerDetailScreen(
                        //  customerId = customerId ?: "",
                        customerId = "1",
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(NavRoute.Credits.path) {
                    InvoiceListScreen(onNavigate = { navController.navigate(NavRoute.Customer.path) })
                }
            }
        }
    }
}
