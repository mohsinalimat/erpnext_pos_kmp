package com.erpnext.pos.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)


sealed class NavRoute(val path: String) {
    object AuthCheck: NavRoute("auth_check")
    object Login: NavRoute("login")
    object Home : NavRoute("home")
    object Inventory : NavRoute("inventory")
    object Sale : NavRoute("sale")
    object Customer : NavRoute("Customer")
    object CustomerDetail : NavRoute("customer_detail")
    object Credits : NavRoute("credits")
}
