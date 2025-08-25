package com.erpnext.pos.views.login

import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.LoginUseCase
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val useCase: LoginUseCase
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.Loading)
    val stateFlow: StateFlow<LoginState> = _stateFlow.asStateFlow()

    fun doLogin(email: String, password: String) {
        executeUseCase(
            action = {
                _stateFlow.update { LoginState.Loading }
                useCase.execute(CredentialsDto(email, password))
                _stateFlow.update { LoginState.Success }
            },
            exceptionHandler = {
                _stateFlow.update { LoginState.InvalidCredentials }
            }
        )
    }
}