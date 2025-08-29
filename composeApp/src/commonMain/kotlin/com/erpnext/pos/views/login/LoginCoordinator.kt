package com.erpnext.pos.views.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

class LoginCoordinator(
    val viewModel: LoginViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun doLogin() {
        viewModel.doLogin()
    }
}

@Composable
fun rememberLoginCoordinator(): LoginCoordinator {
    val viewModel: LoginViewModel = koinInject()

    return remember(viewModel) {
        LoginCoordinator(
            viewModel = viewModel
        )
    }
}