package com.erpnext.pos.remoteSource.oauth

import com.erpnext.pos.remoteSource.dto.LoginInfo
import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.views.login.Site
import io.ktor.client.plugins.auth.providers.*
import kotlinx.coroutines.flow.Flow

interface TokenStore {
    fun load(): TokenResponse?
    suspend fun save(tokens: TokenResponse)
    suspend fun clear()

    // Observable para UI/Logic
    fun tokensFlow(): Flow<TokenResponse?>
}

interface AuthInfoStore {
    fun loadAuthInfo(): MutableList<LoginInfo>
    fun loadAuthInfoByUrl(url: String? = null): LoginInfo
    fun saveAuthInfo(info: LoginInfo)
    fun getCurrentSite(): String?
    fun clearAuthInfo()
}

/**
 * Guardar pkce.verifier y state como transitorio y eliminarlos inmediatamente despues
 * de completar el intercambio de `code` por `token`. Cumplimiento de seguridad
 */
interface TransientAuthStore {
    suspend fun savePkceVerifier(verifier: String)
    suspend fun loadPkceVerifier(): String?
    suspend fun clearPkceVerifier()

    suspend fun saveState(state: String)
    suspend fun loadState(): String?
    suspend fun clearState()
}