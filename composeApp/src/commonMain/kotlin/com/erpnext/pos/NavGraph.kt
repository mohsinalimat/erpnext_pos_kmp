package com.erpnext.pos

import CheckoutScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.views.invoice.InvoiceListScreen
import com.erpnext.pos.views.customer.CustomerRoute
import com.erpnext.pos.views.home.HomeRoute
import com.erpnext.pos.views.inventory.InventoryRoute
import com.erpnext.pos.views.invoice.InvoiceRoute
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
            }
            composable(NavRoute.Home.path) {
                HomeRoute()
            }
            composable(NavRoute.Inventory.path) {
                InventoryRoute()
            }
            composable(NavRoute.Sale.path) {
                CheckoutScreen(onNavigate = { navController.navigate(NavRoute.Customer.path) })
            }
            composable(NavRoute.Customer.path) {
                CustomerRoute()
            }
            /*composable("${NavRoute.CustomerDetail.path}/{customerId}") { backStackEntry ->
                //val customerId = backStackEntry.path<String>("customerId") ?: ""
                CustomerDetailScreen(
                    //  customerId = customerId ?: "",
                    customerId = "1",
                    onBackClick = { navController.popBackStack() }
                )
            }*/
            composable(NavRoute.Credits.path) {
                InvoiceRoute()
            }
        }
    }
}