package com.erpnext.pos.views.login

import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.usecases.LoginUseCase
import com.erpnext.pos.navigation.AuthNavigator
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import com.erpnext.pos.remoteSource.oauth.OAuthConfig
import com.erpnext.pos.remoteSource.oauth.buildAuthorizeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authConfig: OAuthConfig,
    private val authNavigator: AuthNavigator,
    private val oauthService: APIService,
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.Loading)
    val stateFlow: StateFlow<LoginState> = _stateFlow.asStateFlow()

    val authRequest = buildAuthorizeRequest(authConfig)
    fun doLogin() {
        authNavigator.openAuthPage(authRequest.url)
    }

    fun onAuthCodeReceived(code: String) {
        viewModelScope.launch {
            try {
                val tokens = oauthService.exchangeCode(
                    code,
                    authRequest.pkce,
                    authRequest.state,
                    authRequest.state
                )
                _stateFlow.value = LoginState.Success(tokens)
            } catch (e: Exception) {
                _stateFlow.value = LoginState.Error(e.message.toString())
            }
        }
    }
}