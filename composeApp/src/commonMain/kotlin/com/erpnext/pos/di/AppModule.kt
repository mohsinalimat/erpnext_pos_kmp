package com.erpnext.pos.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.erpnext.pos.data.repositories.LoginRepositories
import com.erpnext.pos.domain.usecases.LoginUseCase
import com.erpnext.pos.localSource.AppPreferences
import com.erpnext.pos.localSource.datasources.LoginLocalSource
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.api.createAPIService
import com.erpnext.pos.remoteSource.datasources.LoginRemoteSource
import com.erpnext.pos.views.login.LoginViewModel
import com.erpnext.pos.views.splash.SplashViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.dsl.module

val appModule = module {
    single { HttpClient() }
    single<APIService> {
        Ktorfit.Builder()
            .baseUrl("https://drcr.gruporeal.com/")
            .httpClient(HttpClient { })
            .build().createAPIService()
    }
    single { LoginLocalSource() }
    single { LoginRemoteSource(get()) }
    single { LoginRepositories(get(), get()) }
    single { LoginUseCase(get()) }
    single { LoginViewModel(get()) }
    single { AppPreferences(get()) }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { NavigationManager(get()) }
    single { SplashViewModel(get(), get()) }
    single {
        PreferenceDataStoreFactory.createWithPath {
            "prefs.preferences_pb".toPath()
        }
    }
}