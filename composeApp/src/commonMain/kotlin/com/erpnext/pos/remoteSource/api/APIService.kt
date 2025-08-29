package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.remoteSource.oauth.OAuthConfig
import com.erpnext.pos.remoteSource.oauth.Pkce
import com.erpnext.pos.remoteSource.oauth.TokenStore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.*
import io.ktor.http.*

class APIService(
    private val client: HttpClient,
    private val oauthConfig: OAuthConfig,
    private val store: TokenStore
) {
    private val clientOAuth = client
        .config {
            install(Auth) {
                bearer {
                    loadTokens { store.load() }
                    refreshTokens {
                        val current = store.load() ?: return@refreshTokens null
                        val refreshed =
                            refreshToken(current.refreshToken ?: return@refreshTokens null)
                        val bearer = BearerTokens(
                            refreshed.access_token,
                            refreshed.refresh_token ?: current.refreshToken
                        )
                        store.save(bearer)
                        bearer
                    }
                }
            }
        }

    suspend fun exchangeCode(
        code: String,
        pkce: Pkce,
        expectedState: String,
        returnedState: String
    ): TokenResponse {
        require(expectedState == returnedState) { "CSRF state mismatch" }
        val res = client.post(oauthConfig.tokenUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", oauthConfig.redirectUrl)
                append("client_id", oauthConfig.clientId)
                append("code_verifier", pkce.verifier)
                // Si usas cliente confidencial (no PKCE en mobile), añade client_secret:
                oauthConfig.clientSecret?.let { append("client_secret", it) }
            }.formUrlEncode())
        }.body<TokenResponse>()

        store.save(BearerTokens(res.access_token, res.refresh_token ?: ""))
        return res
    }

    suspend fun refreshToken(refresh: String): TokenResponse {
        return client.post(oauthConfig.tokenUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", refresh)
                append("client_id", oauthConfig.clientId)
                oauthConfig.clientSecret?.let { append("client_secret", it) }
            }.formUrlEncode())
        }.body()
    }

    suspend fun revoke(accessToken: String) {
        client.post(oauthConfig.revokeUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("token=$accessToken")
        }
        store.clear()
    }
}

expect fun defaultEngine(): HttpClientEngine