package com.erpnext.pos.views.home

import com.erpnext.pos.domain.models.POSProfileBO
import com.erpnext.pos.domain.models.UserBO

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val user: UserBO) : HomeState()
    object LoadingProfiles : HomeState()
    data class POSProfiles(val posProfiles: List<POSProfileBO>) : HomeState()
    data class Error(val message: String) : HomeState()
}

data class HomeAction(
    val loadUserInfo: () -> Unit = {},
    val loadPOSProfile: () -> Unit = {},
    val openCashbox: (pos: POSProfileBO) -> Unit = {},
    val onLogout: () -> Unit = {},
    val isCashboxOpen: () -> Boolean = { false },
    val onError: (error: String) -> Unit = {},
)