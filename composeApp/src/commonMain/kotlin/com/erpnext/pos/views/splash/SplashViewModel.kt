package com.erpnext.pos.views.splash

import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.localSource.AppPreferences
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.utils.verifyAuthentication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val preferences: AppPreferences,
    private val navigationManager: NavigationManager,
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<SplashState> = MutableStateFlow(SplashState.Loading)
    val stateFlow: StateFlow<SplashState> = _stateFlow.asStateFlow()

    fun verifyToken() {
        viewModelScope.launch {
            preferences.authToken.collect { value ->
                _stateFlow.update { SplashState.Loading }
                val isAuthenticated = verifyAuthentication(value)
                val direction = if (isAuthenticated) NavRoute.Home else NavRoute.Login
                navigationManager.navigateTo(direction)
                _stateFlow.update { SplashState.Success }
            }
        }
    }
}