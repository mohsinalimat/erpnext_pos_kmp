package com.erpnext.pos

import android.content.Context
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.remoteSource.oauth.TransientAuthStore
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.withLock
import androidx.core.content.edit
import androidx.security.crypto.MasterKey

class AndroidTokenStore(private val context: Context) : TokenStore, TransientAuthStore {
    private val mutex = Mutex()
    private val stateFlow = MutableStateFlow<BearerTokens?>(null)

    private val prefs by lazy {
        val keyGen = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            keyGen,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun stringKey(key: String) = key

    override suspend fun save(tokens: BearerTokens) = mutex.withLock {
        prefs.edit().apply {
            putString(stringKey("access_token"), tokens.accessToken)
            putString(stringKey("refresh_token"), tokens.refreshToken)
            apply()
        }
        stateFlow.value = tokens
    }

    override suspend fun load(): BearerTokens? = mutex.withLock {
        val at = prefs.getString(stringKey("access_token"), null) ?: return null
        val rt = prefs.getString(stringKey("refresh_token"), null) ?: ""
        val tokens = BearerTokens(at, rt)
        stateFlow.value = tokens
        tokens
    }

    override suspend fun clear() = mutex.withLock {
        prefs.edit { clear() }
        stateFlow.value = null
    }

    override fun tokensFlow() = stateFlow.asStateFlow()

    // TransientAuthStore
    override suspend fun savePkceVerifier(verifier: String) {
        prefs.edit { putString("pkce_verifier", verifier) }
    }

    override suspend fun loadPkceVerifier(): String? = prefs.getString("pkce_verifier", null)
    override suspend fun clearPkceVerifier() {
        prefs.edit { remove("pkce_verifier") }
    }

    override suspend fun saveState(state: String) {
        prefs.edit { putString("oauth_state", state) }
    }

    override suspend fun loadState(): String? = prefs.getString("oauth_state", null)
    override suspend fun clearState() {
        prefs.edit { remove("oauth_state") }
    }
}