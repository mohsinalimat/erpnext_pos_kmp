package com.erpnext.pos.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)


@Serializable
sealed class NavRoute(val path: String) {
    @Serializable
    object Splash: NavRoute("splash")
    @Serializable
    object Login: NavRoute("login")
    @Serializable
    object Home : NavRoute("home")
    object Inventory : NavRoute("inventory")
    object Sale : NavRoute("sale")
    object Customer : NavRoute("Customer")
    object CustomerDetail : NavRoute("customer_detail")
    object Credits : NavRoute("credits")
}
