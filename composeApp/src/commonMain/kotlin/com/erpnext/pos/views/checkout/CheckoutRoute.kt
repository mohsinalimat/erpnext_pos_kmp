package com.erpnext.pos.views.checkout

import CheckoutScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutRoute(
    coordinator: CheckoutCoordinator = rememberCheckoutCoordinator()
) {
    val uiState by coordinator.screenStateFlow.collectAsState(CheckoutState.Loading)
    val action = rememberCheckoutActions(coordinator)

    CheckoutScreen(uiState, action)
}

@Composable
fun rememberCheckoutActions(coordinator: CheckoutCoordinator): CheckoutAction {
    return remember(coordinator) {
        CheckoutAction()
    }
}