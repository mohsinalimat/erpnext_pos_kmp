package com.erpnext.pos.views.login

import androidx.lifecycle.viewModelScope
import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.navigation.AuthNavigator
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.remoteSource.oauth.SiteStore
import com.erpnext.pos.remoteSource.oauth.buildAuthorizeRequest
import com.erpnext.pos.remoteSource.oauth.buildOAuthConfig
import com.erpnext.pos.utils.TokenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authNavigator: AuthNavigator,
    private val oauthService: APIService,
    private val authStore: AuthInfoStore,
    private val siteStore: SiteStore,
    private val navManager: NavigationManager
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.Success)
    val stateFlow: StateFlow<LoginState> = _stateFlow.asStateFlow()

    fun doLogin(url: String) {
        authNavigator.openAuthPage(url)
    }

    fun onAuthCodeReceived(code: String) {
        _stateFlow.update { LoginState.Loading }
        viewModelScope.launch {
            try {
                val oauthConfig = buildOAuthConfig(authStore)
                val authRequest = buildAuthorizeRequest(oauthConfig)
                val tokens = oauthService.exchangeCode(
                    oauthConfig.tokenUrl,
                    code,
                    authRequest.pkce,
                    authRequest.state,
                    authRequest.state
                )
                _stateFlow.update { LoginState.Authenticated(tokens) }
            } catch (e: Exception) {
                _stateFlow.update { LoginState.Error(e.message.toString()) }
            }
        }
    }

    fun existingSites(): List<Site>? {
        _stateFlow.update { LoginState.Loading }
        val sites = siteStore.loadSites()
        _stateFlow.update { LoginState.Success }
        return sites
    }

    //TODO: Obtener el URL del Site y luego los Site Info para poder autenticar
    fun onSiteSelected(site: Site) {
        _stateFlow.update { LoginState.Loading }
        try {
            viewModelScope.launch {
                val oauthConfig = buildOAuthConfig(authStore)
                val request = buildAuthorizeRequest(oauthConfig)
                doLogin(request.url)
                _stateFlow.update { LoginState.Success }
            }
        } catch (e: Exception) {
            _stateFlow.update { LoginState.Error(e.message.toString()) }
        }
    }

    fun onAddSite(site: Site) {
        _stateFlow.update { LoginState.Loading }
        viewModelScope.launch {
            val loginInfo = oauthService.getLoginWithSite(site.url)
            authStore.saveAuthInfo(loginInfo)
            siteStore.saveSite(site)
            val oauthConfig = buildOAuthConfig(authStore)
            val request = buildAuthorizeRequest(oauthConfig)
            doLogin(request.url)
            _stateFlow.update { LoginState.Success }
        }
    }

    fun onError(error: String) {
        _stateFlow.update { LoginState.Error(error) }
    }

    fun reset() = _stateFlow.update { LoginState.Success }

    fun isAuthenticated(tokens: TokenResponse) {
        val isAuth = TokenUtils.isValid(tokens.id_token)
        if (isAuth)
            navManager.navigateTo(NavRoute.Home)
        _stateFlow.update { LoginState.Success }
    }
}