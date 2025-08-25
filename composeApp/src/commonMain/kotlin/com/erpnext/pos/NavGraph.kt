package com.erpnext.pos

import CheckoutScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.views.HomeScreen
import com.erpnext.pos.views.InventoryScreen
import com.erpnext.pos.views.InvoiceListScreen
import com.erpnext.pos.views.customer.CustomerDetailScreen
import com.erpnext.pos.views.customer.CustomerListScreen
import com.erpnext.pos.views.login.LoginRoute
import com.erpnext.pos.views.splash.SplashRoute

@ExperimentalMaterial3Api
object NavGraph {

    @Composable
    fun Setup(
        navController: NavHostController,
        isExpandedScreen: Boolean
    ) {
        NavHost(navController, startDestination = NavRoute.Splash.path) {
            composable(NavRoute.Splash.path) {
                SplashRoute()
            }
            composable(NavRoute.Login.path) {
                LoginRoute()
                /*LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Home.path) {
                            popUpTo(NavRoute.Login.path) { inclusive = true }
                        }
                    }
                )*/
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