package com.erpnext.pos.views.login

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRoute(
    coordinator: LoginCoodinator = rememberLoginCoordinator(),
) {
    val uiState by coordinator.screenStateFlow.collectAsState(LoginState.Loading)
    val actions = rememberLoginActions(coordinator)

    LoginScreen(uiState, actions)
}

@Composable
fun rememberLoginActions(coordinator: LoginCoodinator): LoginAction {
    return remember(coordinator) {
        LoginAction(
            onLogin = coordinator::doLogin
        )
    }
}