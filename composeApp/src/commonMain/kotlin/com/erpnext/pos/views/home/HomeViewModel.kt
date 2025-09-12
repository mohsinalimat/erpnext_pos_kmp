package com.erpnext.pos.views.home

import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.usecases.FetchPosProfileUseCase
import com.erpnext.pos.domain.usecases.FetchUserInfoUseCase
import com.erpnext.pos.navigation.NavigationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val navManager: NavigationManager,
    private val fetchUserInfoUseCase: FetchUserInfoUseCase,
    private val fetchPosProfileUseCase: FetchPosProfileUseCase
) : BaseViewModel() {
    private val _stateFlow: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Loading)
    val stateFlow = _stateFlow.asStateFlow()

    init {
        loadPOSProfile()
        loadUserInfo()
    }

    fun loadUserInfo() {
        _stateFlow.update { HomeState.Loading }
        executeUseCase(
            action = {
                val userInfo = fetchUserInfoUseCase.invoke(null)
                _stateFlow.update { HomeState.Success(userInfo) }
            },
            exceptionHandler = {
                _stateFlow.update { state -> HomeState.Error(it.message ?: "Error") }
            }
        )
    }

    fun loadPOSProfile() {
        _stateFlow.update { HomeState.Loading }
        executeUseCase(
            action = {
                val posProfiles = fetchPosProfileUseCase.invoke(null)
                _stateFlow.update { HomeState.POSProfiles(posProfiles) }
            },
            exceptionHandler = {
                _stateFlow.update { state -> HomeState.Error(it.message ?: "Error") }
            }
        )
    }

    fun logout() {}

    fun onError(error: String) {
        _stateFlow.update { HomeState.Error(error) }
    }

    //TODO: Aqui tenemos que abrir la caja
    fun onPosSelected(pos: POSProfileBO) {
        print("POS Selected => $pos")
    }
}