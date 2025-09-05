package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.BuildKonfig
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.LoginInfo
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
    client: HttpClient,
    private val oauthConfig: OAuthConfig,
    private val store: TokenStore
) {
    private val clientOAuth = client
        .config {
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = store.load()
                        if (token != null)
                            BearerTokens(token.access_token, token.refresh_token)
                        null
                    }
                    refreshTokens {
                        val current = store.load() ?: return@refreshTokens null
                        val refreshed =
                            refreshToken(current.refresh_token ?: return@refreshTokens null)
                        val bearer = BearerTokens(
                            refreshed.access_token,
                            refreshed.refresh_token ?: current.refresh_token
                        )
                        store.save(
                            TokenResponse(
                                access_token = refreshed.access_token,
                                refresh_token = refreshed.refresh_token,
                                id_token = refreshed.id_token,
                                expires_in = refreshed.expires_in
                            )
                        )
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
        try {
            require(expectedState == returnedState) { "CSRF state mismatch" }
            val res = clientOAuth.post(oauthConfig.tokenUrl) {
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

            store.save(res)
            return res
        } catch (e: Exception) {
            e.message
            return TokenResponse("", "", 0, "", "")
        }

    }

    suspend fun refreshToken(refresh: String): TokenResponse {
        return clientOAuth.post(oauthConfig.tokenUrl) {
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
        clientOAuth.post(oauthConfig.revokeUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("token=$accessToken")
        }
        store.clear()
    }

    suspend fun items(): List<ItemDto> {
        return clientOAuth.get(Endpoints.Items.url) {
            contentType(ContentType.Application.Json)
        }.body<List<ItemDto>>()
    }

    //TODO: Cuando tenga el API lo cambiamos
    suspend fun getLoginWithSite(site: String): LoginInfo {
        return LoginInfo(
            BuildKonfig.BASE_URL, BuildKonfig.REDIRECT_URI,
            BuildKonfig.CLIENT_ID, BuildKonfig.CLIENT_SECRET, listOf("all", "openid")
        )
        /*return  clientOAuth.get("") {
             contentType(ContentType.Application.Json)
             setBody(site)
         }.body()*/
    }
}

expect fun defaultEngine(): HttpClientEngine