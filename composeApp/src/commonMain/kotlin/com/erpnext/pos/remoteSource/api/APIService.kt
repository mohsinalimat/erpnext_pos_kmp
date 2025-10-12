package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.BuildKonfig
import com.erpnext.pos.remoteSource.dto.BinDto
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.ItemDetailDto
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.ItemPriceDto
import com.erpnext.pos.remoteSource.dto.LoginInfo
import com.erpnext.pos.remoteSource.dto.POSOpeningEntryDto
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.POSProfileSimpleDto
import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.remoteSource.dto.UserDto
import com.erpnext.pos.remoteSource.dto.WarehouseItemDto
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.remoteSource.oauth.OAuthConfig
import com.erpnext.pos.remoteSource.oauth.Pkce
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.remoteSource.oauth.toBearerToken
import com.erpnext.pos.remoteSource.oauth.toOAuthConfig
import com.erpnext.pos.remoteSource.sdk.ERPDocType
import com.erpnext.pos.remoteSource.sdk.Filter
import com.erpnext.pos.remoteSource.sdk.FrappeErrorResponse
import com.erpnext.pos.remoteSource.sdk.FrappeException
import com.erpnext.pos.remoteSource.sdk.Operator
import com.erpnext.pos.remoteSource.sdk.filters
import com.erpnext.pos.remoteSource.sdk.getERPList
import com.erpnext.pos.remoteSource.sdk.getERPSingle
import com.erpnext.pos.remoteSource.sdk.getFields
import com.erpnext.pos.remoteSource.sdk.postERP
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

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

    suspend fun items(warehouse: String, offset: Int, limit: Int): List<ItemDto> {
        return clientOAuth.getERPList(
            doctype = ERPDocType.Item.path,
            fields = ERPDocType.Item.getFields(),
            orderBy = "item_name",
            baseUrl = authStore.getCurrentSite() ?: "",
            offset = offset,
            limit = limit,
            filters = listOf(
                Filter("warehouse", Operator.EQ, warehouse),
                Filter("disabled", Operator.EQ, false)
            )
        )
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

    suspend fun openCashbox(pos: POSOpeningEntryDto) {
        val url = authStore.getCurrentSite()
        return clientOAuth.postERP(
            ERPDocType.POSProfileEntry.path,
            pos,
            url
        )
    }

    suspend fun getPOSProfileDetails(profileId: String): POSProfileDto {
        val url = authStore.getCurrentSite()
        return clientOAuth.getERPSingle(
            doctype = ERPDocType.POSProfile.path,
            name = profileId.encodeURLParameter(),
            baseUrl = url
        )
    }

    suspend fun getPOSProfiles(): List<POSProfileSimpleDto> {
        val url = authStore.getCurrentSite()
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

    suspend fun getInventoryForWarehouse(
        warehouse: String? = null,
        priceList: String? = "Standard Selling",
    ): List<WarehouseItemDto> {
        val url = authStore.getCurrentSite() ?: throw Exception("URL Invalida")

        //Paso 1: Obtener Bins con Stock > 0
        val bins = clientOAuth.getERPList<BinDto>(
            doctype = ERPDocType.Bin.path,
            fields = listOf("item_code", "actual_qty"),
            baseUrl = url,
            limit = 50,
            offset = 0,
            orderBy = "item_code",
        ) {
            if (warehouse != null)
                "warehouse" eq warehouse
            "actual_qty" gt 0.0
        }

        val itemCodes = bins.map { it.itemCode }.distinct()
        if (itemCodes.isEmpty()) return emptyList()

        //Paso 2: Obtener los precios de Item Price
        val prices = clientOAuth.getERPList<ItemPriceDto>(
            doctype = ERPDocType.ItemPrice.path,
            fields = listOf("item_code", "price_list_rate"),
            baseUrl = url,
            limit = itemCodes.size
        ) {
            "item_code" `in` itemCodes
            if (priceList != null)
                "price_list" eq priceList
        }

        val priceMap = prices.associate { it.itemCode!! to it.priceListRate }

        //Paso 3: Combinar
        val result = bins.map { bin ->
            val price =
                priceMap[bin.itemCode] ?: getFallbackRate(itemCode = bin.itemCode, url = url)
            WarehouseItemDto(bin.itemCode, bin.actualQty, price)
        }

        return result
    }

    private suspend fun getFallbackRate(itemCode: String, url: String?): Double {
        val item = clientOAuth.getERPSingle<Map<String, Double>>(
            doctype = ERPDocType.Item.path,
            name = itemCode,
            baseUrl = url
        )
        return item["standard_rate"] ?: 0.0
    }

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Para Facturación: Fetch optimizado stock + precio por item (usa Frappe method)
    suspend fun getItemStockAndPrice(
        itemCode: String,
        warehouse: String,
        priceList: String = "Standard Selling"
    ): ItemDetailDto {
        val url = authStore.getCurrentSite() ?: throw Exception("URL Invalida")
        val endpoint = "$url/api/method/erpnext.stock.get_item_details"

        val response = clientOAuth.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "args" to mapOf(
                        "item_code" to itemCode,
                        "warehouse" to warehouse,
                        "price_list" to priceList,
                        "qty" to 1,  // Para calcular rate unitario
                        "transaction_type" to "selling"  // Para POS ventas
                    )
                )
            )
        }

        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            try {
                val err = json.decodeFromString<FrappeErrorResponse>(bodyText)
                throw FrappeException(err.exception ?: "Error: ${response.status}", err)
            } catch (e: Exception) {
                throw Exception("Error en get_item_details: ${response.status} - $bodyText", e)
            }
        }

        // Parsea "message" del method response
        val parsed = json.parseToJsonElement(bodyText).jsonObject
        val messageElement =
            parsed["message"] ?: throw FrappeException("No 'message' en respuesta: $bodyText")
        return json.decodeFromJsonElement<ItemDetailDto>(messageElement)
    }
}

expect fun defaultEngine(): HttpClientEngine