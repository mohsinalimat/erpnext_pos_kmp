package com.erpnext.pos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.erpnext.pos.navigation.NavRoute
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SplashScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val isChecking = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val authenticated = false //isAuthenticated() // Llama a tu lógica de verificación de token
            isChecking.value = false
            if (authenticated) {
                navController.navigate(NavRoute.Home.path) {
                    popUpTo(NavRoute.AuthCheck.path) { inclusive = true }
                }
            } else {
                navController.navigate(NavRoute.Login.path) {
                    popUpTo(NavRoute.AuthCheck.path) { inclusive = true }
                }
            }
        }
    }

    // Muestra un indicador de carga mientras se verifica
    if (isChecking.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}