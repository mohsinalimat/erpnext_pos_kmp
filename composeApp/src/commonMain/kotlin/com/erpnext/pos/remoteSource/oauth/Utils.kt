package com.erpnext.pos.remoteSource.oauth

import com.erpnext.pos.randomUrlSafe

data class AuthRequest(val url: String, val state: String, val pkce: Pkce)

fun buildAuthorizeRequest(cfg: OAuthConfig, scopeOverride: List<String>? = null): AuthRequest {
    val pkce = PkceFactory.s256()
    val state = randomUrlSafe(32)
    val query = listOf(
        "response_type" to "code",
        "client_id" to cfg.clientId,
        "redirect_uri" to "${cfg.baseUrl}/${cfg.redirectUrl}",
        "scope" to (scopeOverride ?: cfg.scopes).joinToString(" "),
        "state" to state,
        "code_challenge_method" to pkce.method.lowercase(),
        "code_challenge" to pkce.challenge
    ).joinToString("&") { "${it.first}=${encode(it.second)}" }
    return AuthRequest("${cfg.authorizeUrl}?$query", state, pkce)
}

private fun encode(v: String) = v.encodeToByteArray().decodeToString()