package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.BuildKonfig
import com.erpnext.pos.remoteSource.dto.BinDto
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.ContactChildDto
import com.erpnext.pos.remoteSource.dto.CustomerDto
import com.erpnext.pos.remoteSource.dto.ItemDetailDto
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.dto.ItemPriceDto
import com.erpnext.pos.remoteSource.dto.LoginInfo
import com.erpnext.pos.remoteSource.dto.OutstandingInfo
import com.erpnext.pos.remoteSource.dto.POSOpeningEntryDto
import com.erpnext.pos.remoteSource.dto.POSProfileDto
import com.erpnext.pos.remoteSource.dto.POSProfileSimpleDto
import com.erpnext.pos.remoteSource.dto.PendingInvoiceDto
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
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    ): TokenResponse? {
        try {
            print("OAuthDebug - Client ID: ${oauthConfig.clientId}")
            print("OAuthDebug - Code: $code")
            print("OAuthDebug - Redirect URI: ${oauthConfig.redirectUrl}")
            print("OAuthDebug - Code Verifier: ${pkce.verifier}")

            require(expectedState == returnedState) { "CSRF state mismatch" }
            val res = client.post(oauthConfig.tokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", oauthConfig.redirectUrl)
                    append("client_id", oauthConfig.clientId)
                    append("code_verifier", pkce.verifier)
                    oauthConfig.clientSecret?.let { append("client_secret", it) }
                }.formUrlEncode())
            }.body<TokenResponse>()

            store.save(res)
            return res
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
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

    suspend fun getCategories(): List<CategoryDto> {
        val url = authStore.getCurrentSite() ?: ""
        return clientOAuth.getERPList<CategoryDto>(
            ERPDocType.Category.path,
            ERPDocType.Category.getFields(),
            orderBy = "name asc", baseUrl = url
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
            ERPDocType.POSOpeningEntry.path,  // Corrección: Usa POS Opening Entry doctype
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
        return try {
            clientOAuth.getERPList(
                doctype = ERPDocType.POSProfile.path,
                fields = ERPDocType.POSProfile.getFields(),
                baseUrl = url,
                filters = filters {
                    Filter("disabled", Operator.EQ, false)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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

    // Para Inventario Total: Fetch batch con extras
    suspend fun getInventoryForWarehouse(
        warehouse: String?,
        priceList: String? = "Standard Selling",
        offset: Int = 0,
        limit: Int = 20,
    ): List<WarehouseItemDto> {
        val url = authStore.getCurrentSite() ?: throw Exception("URL Invalida")
        require(!warehouse.isNullOrEmpty()) { "Bodega es requerida para la carga de productos" }

        // Fetch Bins
        val bins = clientOAuth.getERPList<BinDto>(
            doctype = ERPDocType.Bin.path,
            fields = ERPDocType.Bin.getFields(),
            limit = limit,
            offset = offset,
            orderBy = "item_code",
            baseUrl = url
        ) {
            "warehouse" eq warehouse
            "actual_qty" gt 0.0
        }

        val itemCodes = bins.map { it.itemCode }.distinct()
        if (itemCodes.isEmpty()) return emptyList()

        // Fetch Items batch con fields extras
        val items = clientOAuth.getERPList<ItemDto>(
            doctype = ERPDocType.Item.path,
            fields = ERPDocType.Item.getFields(),
            limit = itemCodes.size,
            baseUrl = url
        ) {
            "name" `in` itemCodes
        }

        val itemMap = items.associateBy { it.itemCode }

        // Fetch precios batch
        val prices = clientOAuth.getERPList<ItemPriceDto>(
            doctype = ERPDocType.ItemPrice.path,
            fields = ERPDocType.ItemPrice.getFields(),
            limit = itemCodes.size,
            baseUrl = url
        ) {
            "item_code" `in` itemCodes
            if (priceList != null)
                "price_list" eq priceList
        }

        val priceMap = prices.associate { it.itemCode to it.priceListRate }
        val priceCurrency = prices.associate { it.itemCode to it.currency }

        // Combina todo en WarehouseItemDto
        return bins.map { bin ->
            val item = itemMap[bin.itemCode]
                ?: throw FrappeException("Item no encontrado: ${bin.itemCode}")
            val price = priceMap[bin.itemCode] ?: item.standardRate
            val currency = priceCurrency[bin.itemCode] ?: ""
            val barcode = ""  // No en JSON; "" default
            val isStocked = item.isStockItem
            val isService =
                !isStocked || (item.itemGroup == "COMPLEMENTARIOS")  // Infer de group en JSON

            WarehouseItemDto(
                itemCode = bin.itemCode,
                actualQty = bin.actualQty,
                price = price,
                name = item.itemName,
                itemGroup = item.itemGroup,
                description = item.description,
                barcode = barcode,
                image = item.image ?: "",
                discount = 0.0,  // No field; default 0
                isService = isService,
                isStocked = isStocked,
                stockUom = item.stockUom,
                brand = item.brand ?: "",
                currency = currency
            )
        }
    }

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

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
                        "qty" to 1,
                        "transaction_type" to "selling",
                        "doctype" to "POS Invoice",
                        "set_basic_rate" to 1,
                        "ignore_pricing_rule" to 0
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

        val parsed = json.parseToJsonElement(bodyText).jsonObject
        val messageElement =
            parsed["message"] ?: throw FrappeException("No 'message' en respuesta: $bodyText")

        val details = json.decodeFromJsonElement<ItemDetailDto>(messageElement)

        // Procesamiento post-fetch: Ajusta fields según reglas de negocios
        val processedBarcode = ""  // No en response; default ""
        val processedIsStocked = details.isStocked  // De is_stock_item si en response
        val processedIsService = !processedIsStocked || (details.itemGroup == "COMPLEMENTARIOS")

        return details.copy(
            itemCode = details.itemCode ?: itemCode,
            price = details.price,
            name = details.name ?: "",
            barcode = processedBarcode,
            discount = 0.0,
            isStocked = processedIsStocked,
            isService = processedIsService
        )
    }

    suspend fun getCustomers(territory: String): List<CustomerDto> {
        val url = authStore.getCurrentSite()
        return clientOAuth.getERPList(
            ERPDocType.Customer.path,
            ERPDocType.Customer.getFields(),
            baseUrl = url,
            orderBy = "customer_name asc",
            filters = filters {
                "disabled" eq false
                "territory" eq territory
            }
        )
    }

    //Para monto total pendientes y List (method whitelisted)
    suspend fun getCustomerOutstanding(customer: String): OutstandingInfo {
        val url = authStore.getCurrentSite()
        val invoices = clientOAuth.getERPList<PendingInvoiceDto>(
            doctype = ERPDocType.SalesInvoice.path,
            fields = ERPDocType.SalesInvoice.getFields(),
            baseUrl = url,
            filters = filters {
                "customer" eq customer
                "status" `in` listOf("Unpaid", "Overdue")
            }
        )
        val totalOutstanding = invoices.sumOf { it.total - it.paidAmount }
        return OutstandingInfo(totalOutstanding, invoices)
    }


    //Para facturas pendientes (lista simple de overdue)
    suspend fun getPendingInvoices(
        posProfile: String,
        offset: Int = 0,
        limit: Int = 20
    ): List<PendingInvoiceDto> {
        return try {
            val url = authStore.getCurrentSite()
            clientOAuth.getERPList(
                doctype = ERPDocType.SalesInvoice.path,
                fields = ERPDocType.SalesInvoice.getFields(),
                offset = offset,
                limit = limit,
                baseUrl = url,
                filters = filters {
                    "outstanding_amount" gt 0
                    "pos_profile" eq posProfile
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

expect fun defaultEngine(): HttpClientEngine