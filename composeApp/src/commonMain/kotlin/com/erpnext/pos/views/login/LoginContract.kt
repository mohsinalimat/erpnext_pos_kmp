package com.erpnext.pos.views.login

import com.erpnext.pos.remoteSource.dto.TokenResponse

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val tokens: TokenResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

data class LoginAction(
    val onLogin: () -> Unit = { },
    val onError: (error: String) -> Unit = {}
)