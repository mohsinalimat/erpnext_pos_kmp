package com.erpnext.pos.remoteSource.oauth

import io.ktor.client.plugins.auth.providers.*
import kotlinx.coroutines.flow.Flow

interface TokenStore {
    suspend fun load(): BearerTokens?
    suspend fun save(tokens: BearerTokens)
    suspend fun clear()

    // Observable para UI/Logic
    fun tokensFlow(): Flow<BearerTokens?>
}

/**
 * Guardar pkce.verifier y state como transitorio y eliminarlos inmediatamente despues
 * de completar el intercambio de `code` por `token`. Cumplimiento de seguridad
 */
interface TransientAuthStore {
    suspend fun savePkceVerifier(verifier: String)
    suspend fun loadPkceVerifier() : String?
    suspend fun clearPkceVerifier()

    suspend fun saveState(state: String)
    suspend fun loadState(): String?
    suspend fun clearState()
}