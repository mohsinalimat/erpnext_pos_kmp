package com.erpnext.pos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed class NavRoute(
    val path: String,
    val title: String,
    val icon: ImageVector
) {
    object Splash : NavRoute("splash", "Splash", Icons.Filled.Home)
    object Login : NavRoute("login", "Inicio de sesion", Icons.Filled.Home)
    object Home : NavRoute("home", "Inicio", Icons.Filled.Home)
    object Inventory : NavRoute("inventory", "Inventario", Icons.Filled.Inventory2)
    object Sale : NavRoute("sale", "Ventas", Icons.Filled.PointOfSale)
    object Customer : NavRoute("customer", "Clientes", Icons.Filled.People)
    object Credits : NavRoute("credits", "Créditos", Icons.Filled.Receipt)
    // Añade otras rutas si las tienes

    // Si NavRoute SÍ se usa como argumento de navegación y necesitas serializarlo,
    // pero el icono no es parte de ese argumento, podrías hacer esto:
    // @kotlinx.serialization.Transient
    // val icon: ImageVector? = null // O un valor por defecto que no uses
    // Y luego tener una función aparte para obtener el icono real basado en el path/tipo
    // Esto es más complejo y usualmente no necesario si NavRoute es solo un descriptor local.
}
