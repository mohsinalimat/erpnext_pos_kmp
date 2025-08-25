package com.erpnext.pos.views.login

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    object InvalidCredentials : LoginState()
}

data class LoginAction(
    val onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    val onError: (error: String) -> Unit = {}
)