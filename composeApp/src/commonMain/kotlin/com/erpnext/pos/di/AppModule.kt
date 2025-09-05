package com.erpnext.pos.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.erpnext.pos.BuildKonfig
import com.erpnext.pos.data.repositories.InventoryRepository
import com.erpnext.pos.domain.usecases.FetchInventoryItemUseCase
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.api.defaultEngine
import com.erpnext.pos.remoteSource.datasources.InventoryRemoteSource
import com.erpnext.pos.remoteSource.oauth.OAuthConfig
import com.erpnext.pos.views.login.LoginViewModel
import com.erpnext.pos.views.splash.SplashViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {

    single {
        OAuthConfig(
            baseUrl = BuildKonfig.BASE_URL,
            clientId = BuildKonfig.CLIENT_ID,
            clientSecret = BuildKonfig.CLIENT_SECRET,
            redirectUrl = BuildKonfig.REDIRECT_URI,
            scopes = listOf("all", "openid")
        )
    }

    single {
        HttpClient(defaultEngine()) {
            install(ContentNegotiation) { json() }
        }
    }

    single {
        APIService(
            client = get(),
            oauthConfig = get(),
            store = get()
        )
    }

    single { InventoryRemoteSource(get(), get()) }
    single { InventoryRepository(get()) }
    single { FetchInventoryItemUseCase(get()) }
    single { LoginViewModel(get(), get(), get(), get(), get()) }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { NavigationManager(get()) }
    single { SplashViewModel(get(), get()) }
    single {
        PreferenceDataStoreFactory.createWithPath {
            "./prefs.preferences_pb".toPath()
        }
    }
}

fun initKoin(config: KoinAppDeclaration? = null, modules: List<Module> = listOf()) {
    startKoin {
        config?.invoke(this)
        modules(appModule + modules)
    }
}