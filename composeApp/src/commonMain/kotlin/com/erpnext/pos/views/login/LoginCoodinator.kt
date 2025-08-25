package com.erpnext.pos.views.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.koinInject

class LoginCoodinator(
    val viewModel: LoginViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun doLogin(email: String, password: String) {
        viewModel.doLogin(email, password)
    }
}

@Composable
fun rememberLoginCoordinator(): LoginCoodinator {
    val viewModel: LoginViewModel = koinInject()

    return remember(viewModel) {
        LoginCoodinator(
            viewModel = viewModel
        )
    }
}