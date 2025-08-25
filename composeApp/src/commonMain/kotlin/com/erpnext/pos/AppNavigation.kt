package com.erpnext.pos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import com.erpnext.pos.NavGraph.Setup
import com.erpnext.pos.navigation.BottomBarNavigation
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.navigation.NavigationManager
import org.koin.compose.koinInject

fun shouldShowBottomBar(currentRoute: String): Boolean {
    return currentRoute !in listOf(NavRoute.Login.path, NavRoute.Splash.path)
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
            Setup(navController, false)

            val navManager: NavigationManager = koinInject()
            LaunchedEffect(Unit) {
                navManager.navigationEvents.collect { event ->
                    when (event) {
                        is NavRoute.Login -> navController.navigate(NavRoute.Login.path)
                        is NavRoute.Home -> navController.navigate(NavRoute.Home.path)
                        else -> {}
                    }
                }
            }
        }
    }
}
