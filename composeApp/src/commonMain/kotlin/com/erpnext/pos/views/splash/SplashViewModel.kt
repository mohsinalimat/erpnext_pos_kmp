package com.erpnext.pos.views.splash

import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.utils.verifyAuthentication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val navigationManager: NavigationManager,
    private val tokenStore: TokenStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<SplashState> = MutableStateFlow(SplashState.Loading)
    val stateFlow: StateFlow<SplashState> = _stateFlow.asStateFlow()

    fun verifyToken() {
        viewModelScope.launch {
            _stateFlow.update { SplashState.Loading }
            val tokens = tokenStore.load()
            tokens.let { token ->
                val isLoggedIn = verifyAuthentication(token?.accessToken)
                val direction = if (isLoggedIn) NavRoute.Home else NavRoute.Login
                navigationManager.navigateTo(direction)
                _stateFlow.update { SplashState.Success }
            }
        }
    }
}