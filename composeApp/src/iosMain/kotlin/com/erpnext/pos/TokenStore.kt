package com.erpnext.pos

import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.remoteSource.oauth.TransientAuthStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Security.*
import platform.darwin.*

private fun keychainSet(key: String, value: String): Boolean {
    val data = value.cstr.getBytes()
    val query = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrAccount to key,
        kSecValueData to data
    )
    // delete existing
    SecItemDelete(query.toCFDictionary())
    val status = SecItemAdd(query.toCFDictionary(), null)
    return status == errSecSuccessL
}

private fun keychainGet(key: String): String? {
    val query = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrAccount to key,
        kSecReturnData to kCFBooleanTrue,
        kSecMatchLimit to kSecMatchLimitOne
    )
    val resultPtr = nativeHeap.alloc<COpaquePointerVar>()
    val status = SecItemCopyMatching(query.toCFDictionary(), resultPtr.ptr)
    if (status != errSecSuccess) {
        nativeHeap.free(resultPtr)
        return null
    }
    val data =
        resultPtr.value?.reinterpret<NSData>() ?: run { nativeHeap.free(resultPtr); return null }
    val str = NSString.create(data, 0u)!!.toString()
    nativeHeap.free(resultPtr)
    return str
}

private fun keychainDelete(key: String) {
    val query = mapOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrAccount to key
    )
    SecItemDelete(query.toCFDictionary())
}

class IosTokenStore : TokenStore, TransientAuthStore {
    private val mutex = Mutex()
    private val _flow = MutableStateFlow<BearerTokens?>(null)

    private fun saveInternal(key: String, value: String) = keychainSet(key, value)
    private fun loadInternal(key: String) = keychainGet(key)
    private fun deleteInternal(key: String) = keychainDelete(key)

    override suspend fun save(tokens: BearerTokens) = mutex.withLock {
        saveInternal("access_token", tokens.accessToken)
        saveInternal("refresh_token", tokens.refreshToken)
        tokens.expiresAtEpochSeconds?.let { saveInternal("expires_at", it.toString()) }
        _flow.value = tokens
    }

    override suspend fun load(): BearerTokens? = mutex.withLock {
        val at = loadInternal("access_token") ?: return null
        val rt = loadInternal("refresh_token") ?: ""
        val expires = loadInternal("expires_at")?.toLongOrNull()
        val t = BearerTokens(at, rt, expires)
        _flow.value = t
        t
    }

    override suspend fun clear() = mutex.withLock {
        deleteInternal("access_token")
        deleteInternal("refresh_token")
        deleteInternal("expires_at")
        _flow.value = null
    }

    override fun tokensFlow() = _flow.asStateFlow()

    override suspend fun savePkceVerifier(verifier: String) =
        saveInternal("pkce_verifier", verifier)

    override suspend fun loadPkceVerifier(): String? = loadInternal("pkce_verifier")
    override suspend fun clearPkceVerifier() = deleteInternal("pkce_verifier")

    override suspend fun saveState(state: String) = saveInternal("oauth_state", state)
    override suspend fun loadState(): String? = loadInternal("oauth_state")
    override suspend fun clearState() = deleteInternal("oauth_state")
}
