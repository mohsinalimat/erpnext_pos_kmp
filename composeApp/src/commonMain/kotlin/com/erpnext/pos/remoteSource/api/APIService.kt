package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.BuildKonfig
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.LoginInfo
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.remoteSource.dto.UserDto
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.remoteSource.oauth.OAuthConfig
import com.erpnext.pos.remoteSource.oauth.Pkce
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.remoteSource.oauth.toBearerToken
import com.erpnext.pos.remoteSource.oauth.toOAuthConfig
import com.erpnext.pos.remoteSource.sdk.ERPDocType
import com.erpnext.pos.remoteSource.sdk.Filter
import com.erpnext.pos.remoteSource.sdk.Operator
import com.erpnext.pos.remoteSource.sdk.filters
import com.erpnext.pos.remoteSource.sdk.getERPList
import com.erpnext.pos.remoteSource.sdk.getERPSingle
import com.erpnext.pos.remoteSource.sdk.getFields
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
    private val store: TokenStore,
    private val authStore: AuthInfoStore
) {
    private val clientOAuth = client
        .config {
            install(Auth) {
                bearer {
                    loadTokens {
                        store.load()?.toBearerToken()
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
        oauthConfig: OAuthConfig,
        code: String,
        pkce: Pkce,
        expectedState: String,
        returnedState: String
    ): TokenResponse {
        try {
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

            store.save(res)
            return res
        } catch (e: Throwable) {
            e.printStackTrace()
            return TokenResponse("", "", 0, "", "", "")
        }
    }

    suspend fun refreshToken(refresh: String): TokenResponse {
        val currentSite = authStore.getCurrentSite()
        val oauthConfig = authStore.loadAuthInfoByUrl(currentSite!!)
        val config = OAuthConfig(
            oauthConfig.url, oauthConfig.clientId, oauthConfig.clientSecret,
            oauthConfig.redirectUrl, listOf("all", "openid")
        )
        return client.post(config.tokenUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", refresh)
                append("client_id", oauthConfig.clientId)
                oauthConfig.clientSecret.let { append("client_secret", it) }
            }.formUrlEncode())
        }.body()
    }

    suspend fun getUserInfo(): UserDto {
        val url = authStore.getCurrentSite()
        if (url.isNullOrEmpty())
            throw Exception("URL Invalida")

        val userId = store.loadUser()

        if (userId.isNullOrEmpty())
            throw Exception("Usuario Invalido")

        return clientOAuth.getERPSingle(
            ERPDocType.User.path,
            userId,
            url
        )
    }

    suspend fun revoke(accessToken: String) {
        val oAuthConfig = authStore.loadAuthInfoByUrl().toOAuthConfig()
        clientOAuth.post(oAuthConfig.revokeUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("token=$accessToken")
        }
        store.clear()
    }

    suspend fun items(offset: Int, limit: Int): List<ItemDto> {
        return clientOAuth.getERPList(
            doctype = ERPDocType.Item.path,
            fields = ERPDocType.Item.getFields(),
            orderBy = "item_name",
            baseUrl = authStore.getCurrentSite() ?: "",
            offset = offset,
            limit = limit
        ) {
            Filter("disabled", Operator.EQ, false)
        }
    }

    suspend fun getCategories(): List<CategoryDto> {
        val url = authStore.getCurrentSite() ?: ""
        return clientOAuth.getERPList(
            ERPDocType.Category.path,
            ERPDocType.Category.getFields(),
            orderBy = "name", baseUrl = url
        )
    }

    suspend fun getItemDetail(itemId: String): ItemDto {
        val url = authStore.getCurrentSite()
        if (url.isNullOrEmpty())
            throw Exception("URL Invalida")

        return clientOAuth.getERPSingle(
            doctype = ERPDocType.Item.path,
            name = itemId,
            baseUrl = url
        )
    }

    suspend fun getPOSProfileInfo(): List<POSProfileDto> {
        val url = authStore.getCurrentSite()
        if (url.isNullOrEmpty())
            throw Exception("URL Invalida")
        return clientOAuth.getERPList(
            doctype = ERPDocType.POSProfile.path,
            fields = ERPDocType.POSProfile.getFields(),
            baseUrl = url,
            filters = filters {
                Filter("disabled", Operator.EQ, false)
            }
        )
    }

    //TODO: Cuando tenga el API lo cambiamos
    suspend fun getLoginWithSite(site: String): LoginInfo {
        return LoginInfo(
            BuildKonfig.BASE_URL, BuildKonfig.REDIRECT_URI,
            BuildKonfig.CLIENT_ID, BuildKonfig.CLIENT_SECRET, listOf("all", "openid"),
            "ERP-POS Distribuidora Reyes"
        )
        /*return  clientOAuth.get("") {
             contentType(ContentType.Application.Json)
             setBody(site)
         }.body()*/
    }
}

expect fun defaultEngine(): HttpClientEngine